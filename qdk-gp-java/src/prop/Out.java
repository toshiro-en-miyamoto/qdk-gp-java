package prop;

import java.io.*;
import java.nio.charset.*;

class Out {
    static final String LF = "\n";

    static PrintStream utf8 = null;

    static {
        utf8 = System.out;
        try {
            utf8 =
            new PrintStream(System.out, true, StandardCharsets.UTF_8.name());
        } catch(Exception e) {
            ;
        }
    }
}
