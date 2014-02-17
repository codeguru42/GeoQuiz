package codeguru.geoquiz.test;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;
import codeguru.geoquiz.MainActivity;
import java.io.IOException;
import java.io.InputStream;
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
    
    public void testPreconditions() throws IOException {
        Assert.assertNotNull(mActivity);
        
        InputStream in = mActivity.openFileInput(DB_NAME);
        Assert.assertNotNull(in);
        in.close();
    }

}
