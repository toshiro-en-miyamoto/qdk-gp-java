package prop;

import java.util.*;
import java.nio.file.*;

class PropertyFile {
    static final int LEAD_FILE_ID = 0;
    private static int static_file_id = 0;

    private int fileId;
    private final Path path;
    private boolean isAvailable;

    int getFileId() { return fileId; }
    final Path getPath() { return path; }
    boolean isAvailable() { return isAvailable; }

    private PropertyFile(int id, final String filename)
    {
        fileId = id;
        path = Paths.get(filename);
        isAvailable = Files.isReadable(path);
    }

    static void accept(List<PropertyFile> list, final String filename)
    {
        PropertyFile pf = new PropertyFile(static_file_id++, filename);
        if(pf.isAvailable)
            list.add(pf);
    }

    @Override
    public String toString() {
        return fileId + Property.FID_VAL_SEP + path.getFileName().toString();
    }
}
