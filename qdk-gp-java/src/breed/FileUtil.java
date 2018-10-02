package breed;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.TreeMap;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Stream;

class FileUtil
{
    static <K> TreeMap<K,Double> readEntries(
        String filename,
        Function<String,K> keyMapper)
    {
        Path path = Paths.get(filename);
        Properties entries = new Properties();
        try (BufferedReader reader = Files.newBufferedReader(path))
        {
            entries.load(reader);
        } catch (IOException e) {
            System.err.printf(Settings.FILE_READ_ERROR, filename);
        }

        return entries.stringPropertyNames().stream()
            .collect(
                TreeMap::new,
                (m, key) -> {
                    m.put(
                        keyMapper.apply(key),
                        Double.parseDouble(entries.getProperty(key))
                    );
                },
                TreeMap::putAll
            );
    }

    static TreeMap<Integer,Double> readValues(String filename)
    {
        return Stream.of(Paths.get(filename))
            .flatMap(path -> {
                Stream<String> lines;
                try {
                    lines = Files.lines(path, Settings.charset);
                } catch(IOException e) {
                    System.err.printf(Settings.FILE_READ_ERROR, path);
                    lines = Stream.<String> empty();
                }
                return lines;
            })
            .filter(line -> !line.isEmpty())
            .collect(
                TreeMap::new,
                (m, line) -> {
                    m.put(m.size(), Double.parseDouble(line));
                },
                TreeMap::putAll
            );
    }

    public static void main(String[] args)
    {
        double d = Double.parseDouble(args[0]);
        System.out.println(d);

        // TreeMap<Integer,Double> val = readValues(args[0]);
        /**
        TreeMap<Year,Double> val
            = readEntries(args[0], (s) -> Year.of(Integer.parseInt(s)));
        val.entrySet().stream().forEach(e -> System.out.println(e));
        */
    }

}