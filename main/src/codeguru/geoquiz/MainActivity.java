package codeguru.geoquiz;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends ActionBarActivity {

    private static final String TAG = MainActivity.class.getName();

    private static final String DB_NAME = "geoquiz.sqlite";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            copyDatabaseAsset();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void copyDatabaseAsset() throws IOException {
        InputStream in = null;
        OutputStream out = null;

        try {
            in = getAssets().open(DB_NAME);
            out = openFileOutput(DB_NAME, MODE_PRIVATE);

            byte[] buffer = new byte[1024];
            int read;

            // Copy from input stream to output stream
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            if (in != null) {
                in.close();
            }

            if (out != null) {
                out.close();
            }
        }
    }

}
