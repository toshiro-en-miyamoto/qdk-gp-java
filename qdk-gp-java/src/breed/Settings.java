package breed;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

class Settings {
    // shared objects
    static final Charset charset;

    // keys in the settings
    static final String CKEY_CHARSET;
    static final String CKEY_YEARS_FILE;
    static final String CKEY_WEEK_OF_YEAR_FILE;
    static final String CKEY_DAY_OF_WEEK_FILE;
    static final String CKEY_TIME_OF_DAY_FILE;
    static final String CKEY_INTERVAL_IN_MINUTES;
    static final String CKEY_DATETIME_ZONE_OFFSET;
    static final String CKEY_VALUE_NOISE;
    static final String CKEY_VALUE_RATIO;

    static final String CKEY_FILE_TAG;
    static final String CKEY_FILE_EXT;
    static final String CKEY_FILE_NAME_FORMAT;
    static final String CKEY_FILE_RECORD_FORMAT;
    static final String CKEY_FILE_HEADER;

    // shared messages
    static final String FILE_READ_ERROR  = "*** Error: could not read %s***%n";
    static final String FILE_WRITE_ERROR = "*** Error: could not write %s***%n";

    // private objects/messages of this class
    private static final Properties settings;

    private static final String CONFIG_FILENAME = "breed2.properties";
    private static final String DEFAULT_CHARSET = "UTF-8";
    private static final String FILE_UNAVAILABLE =
        "%s is unavailable. Default settings will be effective.%n";

    static {
        Properties defaults = new Properties();

        CKEY_CHARSET = "file.Charset";
        defaults.setProperty(CKEY_CHARSET, "UTF-8");

        CKEY_YEARS_FILE = "filename.Years";
        defaults.setProperty(CKEY_YEARS_FILE, "breed2-years.txt");

        CKEY_WEEK_OF_YEAR_FILE = "filename.WeekOfYear";
        defaults.setProperty(CKEY_WEEK_OF_YEAR_FILE, "breed2-woy.txt");

        CKEY_DAY_OF_WEEK_FILE = "filename.DayOfWeek";
        defaults.setProperty(CKEY_DAY_OF_WEEK_FILE, "breed2-dow.txt");

        CKEY_TIME_OF_DAY_FILE = "filename.TimeOfDay";
        defaults.setProperty(CKEY_TIME_OF_DAY_FILE, "breed2-tod.txt");

        CKEY_INTERVAL_IN_MINUTES = "process.Interval.inMinutes";
        defaults.setProperty(CKEY_INTERVAL_IN_MINUTES,  "30");

        CKEY_DATETIME_ZONE_OFFSET = "dateTime.ZoneOffset";
        defaults.setProperty(CKEY_DATETIME_ZONE_OFFSET, "+09:00");

        CKEY_VALUE_NOISE = "value.Noise";
        defaults.setProperty(CKEY_VALUE_NOISE, "0.05");

        CKEY_VALUE_RATIO = "value.Ratio";
        defaults.setProperty(CKEY_VALUE_RATIO, "1.00");

        CKEY_FILE_TAG = "filename.Tag";
        defaults.setProperty(CKEY_FILE_TAG, "unkown");

        CKEY_FILE_EXT = "filename.Ext";
        defaults.setProperty(CKEY_FILE_EXT, "csv");

        CKEY_FILE_NAME_FORMAT = "filename.NameFormat";
        defaults.setProperty(CKEY_FILE_NAME_FORMAT, "%s-%s.%s");

        CKEY_FILE_RECORD_FORMAT = "file.RecordFormat";
        defaults.setProperty(CKEY_FILE_RECORD_FORMAT,
            "MP:PRODUCT-TYPE:LOCATION;%.2f;%s%n");

        CKEY_FILE_HEADER = "file.Header";
        defaults.setProperty(CKEY_FILE_HEADER,
            "MeteringPointId;Channel;Unit;Value;Timestamp");

        settings = new Properties(defaults);

        Path path = Paths.get(CONFIG_FILENAME);
        Charset cs = Charset.forName(DEFAULT_CHARSET);
        try (BufferedReader reader = Files.newBufferedReader(path, cs))
        {
            settings.load(reader);
        } catch (IOException e) {
            System.err.printf(FILE_UNAVAILABLE, CONFIG_FILENAME);
        }

        charset = Charset.forName(
            settings.getProperty(CKEY_CHARSET, DEFAULT_CHARSET));
    }

    public static String getProperty(String key)
    {
        return settings.getProperty(key);
    }

    public static void main(String[] args)
    {
        settings.stringPropertyNames().stream()
            .map(k -> k + "=" + settings.getProperty(k))
            .forEach(e -> System.out.println(e));
    }
}
