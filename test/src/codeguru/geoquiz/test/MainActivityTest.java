package codeguru.geoquiz.test;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;
import codeguru.geoquiz.MainActivity;
import java.io.File;
import junit.framework.Assert;

public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private static final String DB_NAME = "geoquiz.sqlite";

    private Activity mActivity;

    public MainActivityTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() {
        mActivity = getActivity();
    }

    public void testPreconditions() {
        Assert.assertNotNull(mActivity);

        File dbFile = new File(mActivity.getFilesDir(), DB_NAME);
        Assert.assertTrue(dbFile.exists());
    }

}
