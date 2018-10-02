package prop;

import java.util.*;
import java.util.stream.*;
import java.nio.file.*;
import java.io.*;
import java.nio.charset.*;

public class Blend
{
    public static void main(String[] args)
    {
        List<PropertyFile> propFiles =
            Arrays.stream(args)
            .collect(
                ArrayList<PropertyFile>::new,
                PropertyFile::accept,
                ArrayList<PropertyFile>::addAll
            );

        propFiles.stream()
            .forEach(pf -> Out.utf8.print(pf + Out.LF));

        List<Property> properties =
            propFiles.stream()
            .flatMap(pf -> {
                Stream<String> lines;
                try {
                    lines = Files.lines(pf.getPath(), StandardCharsets.UTF_8);
                } catch(IOException e) {
                    lines = Stream.<String> empty();
                }
                return lines.map(line -> new Property(pf.getFileId(), line));
            })
            .filter(p -> p.getKey() != null)
            .sorted()
            .distinct()
            .collect(Collectors.toList());

        propFiles.stream()
            .filter(pf -> PropertyFile.LEAD_FILE_ID == pf.getFileId())
            .flatMap(pf -> {
                Stream<String> lines;
                try {
                    lines = Files.lines(pf.getPath(), StandardCharsets.UTF_8);
                } catch(IOException e) {
                    lines = Stream.<String> empty();
                }
                return lines.map(line -> new LeadProperty(line));
            })
            .forEach(line -> {
                Out.utf8.print(line + Out.LF);
                if(line.hasKey())
                    properties.stream()
                    .filter(p -> p.getKey().equals(line.getKey()))
                    .forEach(p -> Out.utf8.print(p.toStringFidValue() + Out.LF));
            });
    }
}