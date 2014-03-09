package codeguru.worldtour;

import android.os.AsyncTask;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ParserTask extends AsyncTask<InputStream, Integer, List<Country>> {

    private static final String TAG = ParserTask.class.getName();

    private final MainActivity activity;

    private final KmlParser parser = new KmlParser();

    public ParserTask(MainActivity activity) {
        this.activity = activity;
    }

    @Override
    protected List<Country> doInBackground(InputStream... inStreams) {
        try {
            return parser.parse(inStreams[0]);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            try {
                inStreams[0].close();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(List<Country> countries) {
        this.activity.setCountries(countries);
    }

}
