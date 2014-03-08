package codeguru.geoquiz;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import codeguru.geoquiz.data.Country;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolygonOptions;
import java.io.IOException;
import java.util.List;

public class MainActivity extends ActionBarActivity {

    private static final String TAG = MainActivity.class.getName();

    private static final int PADDING = 100;

    private static final String KML_FILE = "countries_world.kml";

    private MapView mapView;

    private GoogleMap map;

    private List<Country> countries;

    private int countryIndex;

    private Button nextButton;

    private Button prevButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nextButton = (Button) findViewById(R.id.next_button);
        nextButton.setEnabled(false);
        prevButton = (Button) findViewById(R.id.prev_button);
        prevButton.setEnabled(false);

        try {
            MapsInitializer.initialize(this);
        } catch (GooglePlayServicesNotAvailableException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        map = mapView.getMap();
        map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        try {
            new ParserTask(this).execute(getAssets().open(KML_FILE));
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        countryIndex = -1;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void onNextCountry(View view) {
        Log.d(TAG, "countryIndex=" + countryIndex);
        map.clear();

        ++countryIndex;
        countryIndex %= countries.size();
        Country country = countries.get(countryIndex);
        paintCountry(country);
        moveCamera();

        getSupportActionBar().setTitle("GeoQuiz - " + country.name);
    }

    public void onPrevCountry(View view) {
        Log.d(TAG, "countryIndex=" + countryIndex);
        map.clear();

        --countryIndex;
        countryIndex %= countries.size();
        Country country = countries.get(countryIndex);
        paintCountry(country);
        moveCamera();

        getSupportActionBar().setTitle("GeoQuiz - " + country.name);
    }

    private void moveCamera() {
        LatLngBounds bounds = getBounds(countries.get(countryIndex));
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, PADDING));
    }

    private LatLngBounds getBounds(Country country) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for (List<LatLng> border : country.borders) {
            for (LatLng point : border) {
                builder.include(point);
            }
        }

        return builder.build();
    }

    private void paintCountry(Country country) {
        for (List<LatLng> border : country.borders) {
            PolygonOptions polygonOptions = new PolygonOptions();
            polygonOptions.addAll(border);
            polygonOptions.strokeColor(Color.RED);
            polygonOptions.strokeWidth((float) 0.30);
            polygonOptions.fillColor(Color.BLUE);
            map.addPolygon(polygonOptions);
        }
    }

    public void setCountries(List<Country> countries) {
        this.countries = countries;
        nextButton.setEnabled(true);
        prevButton.setEnabled(true);
    }

}
