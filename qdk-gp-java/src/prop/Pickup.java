package prop;

import java.io.IOException;
import java.util.*;
import java.util.regex.*;
import java.util.stream.*;
import java.nio.charset.*;
import java.nio.file.*;

public class Pickup
{
    public static void main(String[] args)
    {
        if(args.length != 1) {
            System.out.println("Usage Pickup file");
            return;
        }

        List<String> collectedStrings =
            Stream.of(Paths.get(args[0]))
            .flatMap(path -> {
                Stream<String> lines;
                try {
                    lines = Files.lines(path, StandardCharsets.UTF_8);
                } catch(IOException e) {
                    System.err.println("Could not read " + path);
                    lines = Stream.<String> empty();
                }
                return lines;
            })
            .collect(
                ArrayList<String>::new,
                (list, line) -> {
                    Matcher matcher = Property.FID_VAL_REGEX.matcher(line);
                    if(matcher.find()) {
                        if(!list.isEmpty()) {
                            String lastElement = list.remove(list.size() - 1);
                            Property workingProp = new Property(lastElement);
                            workingProp.setValue(matcher.group(2));
                            list.add(workingProp.toString());
                        }
                    } else {
                        list.add(line);
                    }
                },
                ArrayList<String>::addAll
            );
    
        collectedStrings
            .stream()
            .forEach(o -> Out.utf8.print(o + Out.LF));
    }
}