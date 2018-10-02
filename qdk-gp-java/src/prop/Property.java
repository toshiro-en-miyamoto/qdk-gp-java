package prop;

import java.util.regex.*;

class Property implements Comparable<Property>
{
    static final String KEY_VAL_SEP = "=";
    static final String FID_VAL_SEP = ":";

    static final Pattern KEY_VAL_REGEX = Pattern.compile("^([^=]+)=(.*)$");
    static final Pattern FID_VAL_REGEX = Pattern.compile("^(\\d):(.*)$");

    private int fileId;
    private String key;
    private String value;

    int getFileId() { return fileId; }
    final String getKey() { return key; }
    final String getValue() { return value; }
    void setValue(final String value) { this.value = value; }

    Property(final String line)
    {
        this(0, line);
    }

    Property(int fileId, final String line)
    {
        Matcher matcher = KEY_VAL_REGEX.matcher(line);
        if(matcher.find()) {
            key = matcher.group(1);
            value = matcher.group(2);
        } else {
            // this block read comment and blank lines
            key = null;
            value = line;
        }
        this.fileId = fileId;
    }

    // Stream.distinct() depends on this
    @Override
    public boolean equals(Object other)
    {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof Property))return false;
        Property o = (Property)other;
        if(key == null || o.key == null)
            return value.equals(o.value);
        else
            return key.equals(o.key) && value.equals(o.value);
    }

    // Stream.sorted() depends on this
    // (this, other) such that this.compare(other) <= 0
    @Override
    public int compareTo(Property other)
    {
        if(this.key.equals(other.key))
            return this.fileId - other.fileId;
        else
            return this.key.compareTo(other.key);
    }
    
    @Override
    public String toString()
    {
        return key + KEY_VAL_SEP + value;
    }

    final String toStringFidValue()
    {
        return fileId + FID_VAL_SEP + value;
    }
}