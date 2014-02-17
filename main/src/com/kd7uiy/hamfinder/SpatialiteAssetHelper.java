package com.kd7uiy.hamfinder;

import android.content.Context;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import jsqlite.Database;

public class SpatialiteAssetHelper {
    private static final String TAG = SpatialiteAssetHelper.class.getName();
    private static final String ASSET_DB_PATH = "databases";

    private final Context mContext;
    private final String mName;
    private Database mDatabase = null;
    private boolean mIsInitializing = false;

    private boolean mVerbose = false;

    private String mDatabasePath;
    private final String mArchivePath;

    public SpatialiteAssetHelper(Context context, String name,
            String storageDirectory, int version) {
        if (version < 1) {
            throw new IllegalArgumentException("Version must be >= 1, was "
                    + version);
        }
        if (name == null) {
            throw new IllegalArgumentException("Databse name cannot be null");
        }

        mContext = context;
        mName = name;
        mArchivePath = ASSET_DB_PATH + "/" + name + ".zip";
        if (storageDirectory != null) {
            mDatabasePath = storageDirectory;
        } else {
            mDatabasePath = context.getApplicationInfo().dataDir + "/databases";
        }
    }

    public void setVerbose(boolean verbose) {
        mVerbose = verbose;
    }

    /**
     * Create and/or open a database that will be used for reading and writing.
     * The first time this is called, the database will be extracted and copied
     * from the application's assets folder.
     * 
     * <p>
     * Once opened successfully, the database is cached, so you can call this
     * method every time you need to write to the database. (Make sure to call
     * {@link #close} when you no longer need the database.) Errors such as bad
     * permissions or a full disk may cause this method to fail, but future
     * attempts may succeed if the problem is fixed.
     * </p>
     * 
     * <p class="caution">
     * Database upgrade may take a long time, you should not call this method
     * from the application main thread, including from
     * {@link android.content.ContentProvider#onCreate
     * ContentProvider.onCreate()}.
     * 
     * @throws SQLiteException
     *             if the database cannot be opened for writing
     * @return a read/write database object valid until {@link #close} is called
     */
    public synchronized Database getWritableDatabase() {
        if (mDatabase != null) {
            return mDatabase; // The database is already open for business
        }

        if (mIsInitializing) {
            throw new IllegalStateException(
                    "getWritableDatabase called recursively");
        }

        // If we have a read-only database open, someone could be using it
        // (though they shouldn't), which would cause a lock to be held on
        // the file, and our attempts to open the database read-write would
        // fail waiting for the file lock. To prevent that, we acquire the
        // lock on the read-only database, which shuts out other users.

        boolean success = false;
        Database db = null;
        // if (mDatabase != null) mDatabase.lock();
        try {
            mIsInitializing = true;
            db = createOrOpenDatabase(false);
            success = true;
            return db;
        } catch (jsqlite.Exception e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            mIsInitializing = false;
            if (success) {
                if (mDatabase != null) {
                    try {
                        mDatabase.close();
                    } catch (Exception e) {
                    }
                    // mDatabase.unlock();
                }
                mDatabase = db;
            } else {
                // if (mDatabase != null) mDatabase.unlock();
                if (db != null) {
                    try {
                        db.close();
                    } catch (jsqlite.Exception e) {
                    }
                }
            }
        }
        return db;

    }

    /**
     * Create and/or open a database. This will be the same object returned by
     * {@link #getWritableDatabase} unless some problem, such as a full disk,
     * requires the database to be opened read-only. In that case, a read-only
     * database object will be returned. If the problem is fixed, a future call
     * to {@link #getWritableDatabase} may succeed, in which case the read-only
     * database object will be closed and the read/write object will be returned
     * in the future.
     * 
     * <p class="caution">
     * Like {@link #getWritableDatabase}, this method may take a long time to
     * return, so you should not call it from the application main thread,
     * including from {@link android.content.ContentProvider#onCreate
     * ContentProvider.onCreate()}.
     * 
     * @throws SQLiteException
     *             if the database cannot be opened
     * @return a database object valid until {@link #getWritableDatabase} or
     *         {@link #close} is called.
     */
    public synchronized Database getReadableDatabase() {
        if (mDatabase != null) {
            return mDatabase; // The database is already open for business
        }

        if (mIsInitializing) {
            throw new IllegalStateException(
                    "getReadableDatabase called recursively");
        }

        try {
            return getWritableDatabase();
        } catch (SQLiteException e) {
            if (mVerbose) {
                Log.e(TAG, "Couldn't open " + mName
                        + " for writing (Will try read only)", e);
            }
        }

        Database db = null;
        try {
            mIsInitializing = true;
            String path = mContext.getDatabasePath(mName).getPath();
            db = new Database();
            db.open(path, jsqlite.Constants.SQLITE_OPEN_READONLY);

            if (mVerbose) {
                Log.w(TAG, "Opened " + mName + " in read-only mode");
            }
            mDatabase = db;
            return mDatabase;
        } catch (jsqlite.Exception e) {
        } finally {
            mIsInitializing = false;
            if (db != null && db != mDatabase) {
                try {
                    db.close();
                } catch (jsqlite.Exception e) {
                    if (mVerbose) {
                        Log.d(TAG, "Unable to close db");
                    }
                }
            }
        }
        return db;
    }

    private Database createOrOpenDatabase(boolean force)
            throws jsqlite.Exception {
        Database db = returnDatabase();
        if (db != null) {
            // database already exists
            if (force) {
                if (mVerbose) {
                    Log.w(TAG, "forcing database upgrade!");
                }
                copyDatabaseFromAssets();
                db = returnDatabase();
            }
            return db;
        } else {
            // database does not exist, copy it from assets and return it
            copyDatabaseFromAssets();
            db = returnDatabase();
            return db;
        }
    }

    private Database returnDatabase() {
        try {
            Database db = new Database();
            db.open(mDatabasePath + "/" + mName,
                    jsqlite.Constants.SQLITE_OPEN_READWRITE);
            if (mVerbose) {
                Log.i(TAG, "successfully opened database " + mName);
            }
            return db;
        } catch (jsqlite.Exception e) {
            Log.w(TAG,
                    "could not open database " + mName + " - " + e.getMessage());
            return null;
        }
    }

    private void copyDatabaseFromAssets() throws jsqlite.Exception {
        if (mVerbose) {
            Log.w(TAG, "copying database from assets...");
        }

        try {
            InputStream zipFileStream = mContext.getAssets().open(mArchivePath);
            File f = new File(mDatabasePath + "/");
            if (!f.exists()) {
                f.mkdir();
            }

            ZipInputStream zis = getFileFromZip(zipFileStream);
            if (zis == null) {
                throw new jsqlite.Exception(
                        "Archive is missing a SQLite database file");
            }
            writeExtractedFileToDisk(zis, new FileOutputStream(mDatabasePath
                    + "/" + mName));

            Log.w(TAG, "database copy complete");

        } catch (FileNotFoundException fe) {
            jsqlite.Exception se = new jsqlite.Exception("Missing "
                    + mArchivePath
                    + " file in assets or target folder not writable");
            se.setStackTrace(fe.getStackTrace());
            throw se;
        } catch (IOException e) {
            jsqlite.Exception se = new jsqlite.Exception("Unable to extract "
                    + mArchivePath + " to data directory");
            se.setStackTrace(e.getStackTrace());
            throw se;
        }
    }

    private void writeExtractedFileToDisk(ZipInputStream zin, OutputStream outs)
            throws IOException {
        byte[] buffer = new byte[1024];
        int length;
        while ((length = zin.read(buffer)) > 0) {
            outs.write(buffer, 0, length);
        }
        outs.flush();
        outs.close();
        zin.close();
    }

    private ZipInputStream getFileFromZip(InputStream zipFileStream)
            throws FileNotFoundException, IOException {
        ZipInputStream zis = new ZipInputStream(zipFileStream);
        ZipEntry ze = null;
        while ((ze = zis.getNextEntry()) != null) {
            if (mVerbose) {
                Log.w(TAG, "extracting file: '" + ze.getName() + "'...");
            }
            return zis;
        }
        return null;
    }

    protected void closeDb() {
        try {
            mDatabase.close();
        } catch (jsqlite.Exception e) {
            // Do nothing, the Database is already closed
        }
        mDatabase = null;
    }

}
