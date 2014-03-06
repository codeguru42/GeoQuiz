package codeguru.geoquiz;

import android.util.Log;
import android.util.Xml;
import codeguru.geoquiz.data.Country;
import codeguru.geoquiz.data.LookAt;
import com.google.android.gms.maps.model.LatLng;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class KmlParser {

    private static final String TAG = KmlParser.class.getName();

    public List<Country> parse(InputStream in) throws IOException,
            XmlPullParserException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return parseCountries(parser);
        } finally {
            in.close();
        }
    }

    private List<Country> parseCountries(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        List<Country> countries = new ArrayList<Country>();

        Log.d(TAG, parser.getName());
        parser.require(XmlPullParser.START_TAG, null, "kml");
        parser.next();

        Log.d(TAG, parser.getName());
        parser.require(XmlPullParser.START_TAG, null, "Document");

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("Placemark")) {
                countries.add(parseCountry(parser));
            } else {
                skip(parser);
            }
        }

        return countries;
    }

    private Country parseCountry(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "Placemark");

        Country country = new Country();

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("name")) {
                country.name = parseName(parser);
            } else if (name.equals("LookAt")) {
                country.lookAt = parseLookAt(parser);
            } else if (name.equals("MultiGeometry")) {
                country.border = parseBorder(parser);
            } else {
                skip(parser);
            }
        }

        parser.require(XmlPullParser.END_TAG, null, "Placemark");

        return country;
    }

    private void skip(XmlPullParser parser) {

    }

    private String parseName(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "name");
        String name = parseText(parser);
        parser.require(XmlPullParser.END_TAG, null, "name");
        return name;
    }

    private LookAt parseLookAt(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "LookAt");

        LookAt lookAt = new LookAt();
        double longitude = Double.NaN;
        double latitude = Double.NaN;

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("longitude")) {
                longitude = parseLongitude(parser);
            } else if (name.equals("latitude")) {
                latitude = parseLatitude(parser);
            } else if (name.equals("heading")) {
                lookAt.bearing = parseBearing(parser);
            } else if (name.equals("tilt")) {
                lookAt.tilt = parstTilt(parser);
            } else if (name.equals("range")) {
                lookAt.zoom = parseZoom(parser);
            } else {
                skip(parser);
            }
        }

        parser.require(XmlPullParser.END_TAG, null, "Placemark");

        lookAt.target = new LatLng(latitude, longitude);
        return lookAt;
    }

    private List<LatLng> parseBorder(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "name");
        String borderStr = parseText(parser);
        parser.require(XmlPullParser.END_TAG, null, "name");

        List<LatLng> border = new ArrayList<LatLng>();
        String[] coordsStrs = borderStr.split(" ");
        for (String coordsStr : coordsStrs) {
            String[] coords = coordsStr.split(",");
            double longitude = Double.parseDouble(coords[0]);
            double latitude = Double.parseDouble(coords[1]);
            LatLng point = new LatLng(latitude, longitude);
            border.add(point);
        }

        return border;
    }

    private String parseText(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        String result = null;
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;    }

    private double parseLongitude(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "longitude");
        String longitude = parseText(parser);
        parser.require(XmlPullParser.END_TAG, null, "longitude");
        return Double.parseDouble(longitude);
    }

    private double parseLatitude(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "latitude");
        String latitude = parseText(parser);
        parser.require(XmlPullParser.END_TAG, null, "latitude");
        return Double.parseDouble(latitude);
    }

    private float parseBearing(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "heading");
        String bearing = parseText(parser);
        parser.require(XmlPullParser.END_TAG, null, "heading");
        return Float.parseFloat(bearing);
    }

    private float parstTilt(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "tilt");
        String tilt = parseText(parser);
        parser.require(XmlPullParser.END_TAG, null, "tilt");
        return Float.parseFloat(tilt);
    }

    private float parseZoom(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "range");
        String zoom = parseText(parser);
        parser.require(XmlPullParser.END_TAG, null, "range");
        return Float.parseFloat(zoom);
    }

}
