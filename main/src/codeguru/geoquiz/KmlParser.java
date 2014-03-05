package codeguru.geoquiz;

import android.util.Xml;
import codeguru.geoquiz.data.Country;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class KmlParser {

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

    private List<Country> parseCountries(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<Country> countries = new ArrayList<Country>();

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

    private Country parseCountry(XmlPullParser parser) {
        return null;
    }

    private void skip(XmlPullParser parser) {

    }

}
