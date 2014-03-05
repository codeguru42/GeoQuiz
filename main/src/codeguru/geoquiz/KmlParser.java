package codeguru.geoquiz;

import android.util.Xml;
import codeguru.geoquiz.data.Country;
import java.io.IOException;
import java.io.InputStream;
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

    private List<Country> parseCountries(XmlPullParser parser) {
        return null;
    }

}
