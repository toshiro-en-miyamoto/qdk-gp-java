package prop;

import java.util.regex.*;

class LeadProperty
{
    private String key;
    private String line;

    final String getKey() { return key; }
    boolean hasKey() { return key != null; }

    LeadProperty(final String line)
    {
        this.line = line;

        Matcher matcher = Property.KEY_VAL_REGEX.matcher(line);
        if(matcher.find()) {
            key = matcher.group(1);
        } else {
            key = null;
        }
    }

    @Override
    public String toString()
    {
        return key == null ? line : key + Property.KEY_VAL_SEP;
    }
}
