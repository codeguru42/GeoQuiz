package codeguru.geoquiz;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import codeguru.geoquiz.data.Country;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.xmlpull.v1.XmlPullParserException;

public class MainActivity extends ActionBarActivity {

    private static String TAG = MainActivity.class.getName();

    KmlParser parser = new KmlParser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        GoogleMap map = mapFragment.getMap();

        List<Country> countries = getCountries();
        LatLng point = countries.get(1).lookAt.target;

        map.animateCamera(CameraUpdateFactory.newLatLng(point));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private List<Country> getCountries() {
        InputStream in = null;

        try {
            in = getAssets().open("countries_world.kml");
            return parser.parse(in);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (XmlPullParserException e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }

        return null;
    }

}
