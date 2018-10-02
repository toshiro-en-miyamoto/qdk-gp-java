package breed;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class Main
{
    public static void main(String[] args)
    {
        // Input:
        //      String ynYears
        // Output:
        //      Map.Entry<Year,Double>
        //          e.g., [{Year.of(2016), 1.0d}]
        String fnYears = Settings.getProperty(Settings.CKEY_YEARS_FILE);
        Map<Year,Double> mapYears = FileUtil.readEntries(
            fnYears,
            (keyYear) -> Year.of(Integer.parseInt(keyYear))
        );

        // Input:
        //      String fnWOY
        // Output:
        //      Map<Integer,Double> mapWeekOfYear = readValues(fnWOY)
        //          where Integer keyWeek = [0..52]
        //          e.g.,  [{ 0, 1.00d},
        //                  { 1, 1.01d},...
        //                  {51, 1.51d},
        //                  {52, 1.52d}]
        String fnWOY = Settings.getProperty(Settings.CKEY_WEEK_OF_YEAR_FILE);
        Map<Integer,Double> mapWeekOfYear = FileUtil.readValues(fnWOY);

        // Input:
        //      String fnDOW
        // Intermediate work products:
        //      Map<Integer,Double> readValues(fnDOW)
        //          where Integer keyDate = [1..7]
        //          e.g.,  [{DayOfWeek.MONDAY  (1), 2.1d},
        //                  {DayOfWeek.TUESDAY (2), 2.2d},...
        //                  {DayOfWeek.SUNDAY  (7), 2.7d}]
        // Output:
        //      Map<Integer,Double> dayOfTwoWeeksMap
        //          where Integer keyDate = [1..7,8..14]
        //          e.g.,  [{DayOfWeek.MONDAY  (1), 2.1d},
        //                  {DayOfWeek.TUESDAY (2), 2.2d},...
        //                  {DayOfWeek.SUNDAY  (7), 2.7d}]
        //                  {DayOfWeek.MONDAY  +7 (8), 2.1d},
        //                  {DayOfWeek.TUESDAY +7 (9), 2.2d},...
        //                  {DayOfWeek.SUNDAY  +7 (14), 2.7d}]
        // Usage:
        //      let LocalDate date = '2016-01-08' (it's FRIDAY), then
        //          dayOfTwoWeeksMap.entrySet().stream()
        //              .skip(date.getDayOfWeek())
        //              .limit(7)
        //      will produce
        //          Stream<Map.Entry<Integer,Double>>
        //          e.g.,  [{DayOfWeek.FRIDAY       (5), 2.5d},
        //                  {DayOfWeek.SATURDAY     (6), 2.6d},...
        //                  {DayOfWeek.SUNDAY       (7), 2.7d}]
        //                  {DayOfWeek.MONDAY    +7 (8), 2.1d},
        //                  {DayOfWeek.TUESDAY   +7 (9), 2.2d},
        //                  {DayOfWeek.WEDNESDAY +7 (10), 2.3d}]
        //                  {DayOfWeek.THURSDAY  +7 (11), 2.4d}]
        String fnDOW = Settings.getProperty(Settings.CKEY_DAY_OF_WEEK_FILE);
        Map<Integer,Double> mapDayOfTwoWeeks = FileUtil.readValues(fnDOW);
        new TreeMap<Integer,Double>(mapDayOfTwoWeeks).entrySet().stream()
            .forEach(e -> 
                mapDayOfTwoWeeks.put(
                    e.getKey() + 7, e.getValue()
                )
            );

        // Input:
        //      String fnTOD
        // Output:
        //      Map<Integer,Double> mapTimeOfDay = readValues(fnTOD)
        //          where Integer time[0..n]
        //          where n * intervalInMinutes = 24 * 60 (hours per day)
        //              e.g., let intervalInMinutes = 30, then
        //                 [{ 0, 1.00d},
        //                  { 1, 1.01d},
        //                  { 2, 1.02d},...
        //                  {46, 1.46d},
        //                  {47, 1.47d}]
        String fnTOD = Settings.getProperty(Settings.CKEY_TIME_OF_DAY_FILE);
        Map<Integer,Double> mapTimeOfDay = FileUtil.readValues(fnTOD);

        // Input:
        //      Map<Year,   Double> mapYears
        //      Map<Integer,Double> mapWeekOfYear
        //      Map<Integer,Double> mapDayOfTwoWeeks
        //      Map<Integer,Double> mapTimeOfDay
        // Output:
        //      Files that contains Map.Entry<OffsetDateTime,Double>
        //          each file contains records for a day
        //          file name is "${tag}-${yyyy-mm-dd}.${ext}"
        mapYears.entrySet().stream()
            .forEach(entryYear -> {
                breed(
                    entryYear,
                    mapWeekOfYear,
                    mapDayOfTwoWeeks,
                    mapTimeOfDay
                );
            });
    }

    static void breed(
        Map.Entry<Year,Double> year,
        Map<Integer,Double> mapWeekOfYear,
        Map<Integer,Double> mapDayOfTwoWeeks,
        Map<Integer,Double> mapTimeOfDay
    )
    {
        int intervalInMinutes
            = Integer.parseInt(Settings.getProperty(Settings.CKEY_INTERVAL_IN_MINUTES));

        ZoneOffset offset
            = ZoneOffset.of(Settings.getProperty(Settings.CKEY_DATETIME_ZONE_OFFSET));

        double valueNoise
            = Double.parseDouble(Settings.getProperty(Settings.CKEY_VALUE_NOISE));
        // provided that valueNoise = 0.05d
        // (0.95d+0.1*rand.nextDouble()
        double noiseBase = 1.0d - valueNoise;
        double noiseRate = 2 * valueNoise;

        double valueRatio
            = Double.parseDouble(Settings.getProperty(Settings.CKEY_VALUE_RATIO));

        String tag = Settings.getProperty(Settings.CKEY_FILE_TAG);
        String ext = Settings.getProperty(Settings.CKEY_FILE_EXT);
        String filenameFormat = Settings.getProperty(Settings.CKEY_FILE_NAME_FORMAT);
        String recordFormat = Settings.getProperty(Settings.CKEY_FILE_RECORD_FORMAT);
        String header = Settings.getProperty(Settings.CKEY_FILE_HEADER);

        java.util.Random rand = new java.util.Random();

        // Input:
        //      Map.Entry<Year,Double> entryYear
        //          e.g., {keyYear:2016, valYear:1.0d}
        Map<LocalDate,List<Map.Entry<OffsetDateTime,Double>>> records
        = Stream.of(year)

            // Output stream:
            //      Stream<> of entryWeekDay1 : Map.Entry<LocalDate,Double>
            //          where LocalDate day1_of_week = keyYear.atDay(7*keyWeek + 1)
            //          .eg.,  [{keyDate:2016-01-01, valDate:1.00d},
            //                  {keyDate:2016-01-08, valDate:1.01d},....
            //                  {keyDate:2016-12-30, valDate:1.52d}]
            .flatMap(entryYear ->
                AlgebraVector.outerProduct(
                    entryYear,
                    mapWeekOfYear,
                    (keyYear,keyWeek) -> keyYear.atDay(7*keyWeek + 1),
                    (valYear,valWeek) -> valYear * valWeek
                )
            )

            // Output stream:
            //      Stream<> of entryDate : <Map.Entry<LocalDate,Double>
            //          where LocalDate d = keyWeekDay1.plusDays(0..6)
            //          .eg.,  [{keyDate:2016-01-01, valDate:1.000d},
            //                  {keyDate:2016-01-02, valDate:1.001d},
            //                  {keyDate:2016-01-03, valDate:1.002d},...,
            //                  {keyDate:2016-12-30, valDate:1.364d},
            //                  {keyDate:2016-12-31, valDate:1.365d},
            //                  {keyDate:2017-01-01, valDate:1.366d},...,
            //                  {keyDate:2017-01-05, valDate:1.370d}]
            .flatMap(entryWeekDay1 -> {
                // suppose the entryWeekDay1.key is '2016-01-08' FRIDAY (5)
                // then index = 5
                int index = entryWeekDay1.getKey().getDayOfWeek().getValue() - 1;
                return AlgebraVector.outerProduct(
                    entryWeekDay1,
                    mapDayOfTwoWeeks.entrySet().stream()
                        .skip(index)
                        .limit(7),
                        // [{DayOfWeek.FRIDAY       (5), 2.5d},
                        //  {DayOfWeek.SATURDAY     (6), 2.6d},
                        //  {DayOfWeek.SUNDAY       (7), 2.7d}]
                        //  {DayOfWeek.MONDAY    +7 (8), 2.1d},
                        //  {DayOfWeek.TUESDAY   +7 (9), 2.2d},
                        //  {DayOfWeek.WEDNESDAY +7 (10), 2.3d},
                        //  {DayOfWeek.THURSDAY  +7 (11), 2.4d}]
                    (keyWeekDay1,keyDayOfWeek)
                        -> keyWeekDay1.plusDays(keyDayOfWeek - index),
                        //  keyDayOfWeek (4) - index (4) = 0
                    (valWeekDay1,valDayOfWeek)
                        -> valWeekDay1 * valDayOfWeek
                );
            })

            // Output stream:
            //      Stream<> of entryDate : <Map.Entry<LocalDate,Double>
            //          where LocalDate d = keyWeekDay1.plusDays(0..6)
            //          .eg.,  [{keyDate:2016-01-01, valDate:1.000d},
            //                  {keyDate:2016-01-02, valDate:1.001d},
            //                  {keyDate:2016-01-03, valDate:1.002d},...,
            //                  {keyDate:2016-12-30, valDate:1.364d},
            //                  {keyDate:2016-12-31, valDate:1.365d}]
            .limit(year.getKey().length())

            // Output stream:
            //      Stream<> of entryDateTime : Map.Entry<OffsetDateTime,Double>
            //          where OffsetDateTime d = OffsetDateTime
            //                  .of(entryDate.atTime(0, 0, 0), offset)
            //                  .plusMinutes((keyTime + 1) * intervalInMinutes)
            //          when valueNoise = 0.05d, Double value
            //                  = (0.95d + 0.1*Random[0..1]) * valDate * valTime
            .flatMap(entryDate ->
                AlgebraVector.outerProduct(
                    entryDate,
                    mapTimeOfDay,
                    (keyDate,keyTime) -> OffsetDateTime
                        .of(keyDate,
                            LocalTime.ofSecondOfDay((keyTime + 1) * intervalInMinutes * 60),
                            offset),
                    (valDate,valTime) -> 
                        valDate * valTime * (noiseBase + noiseRate * rand.nextDouble())
                )
            )

            // Output:
            //      Map<LocalDate, List<Map.Entry<OffsetDateTime,Double>>>
            .collect(Collectors.groupingBy(e -> e.getKey().toLocalDate()));

        records.entrySet().stream()
            .forEach(entry -> {
                String filename = String.format(
                    filenameFormat,
                    tag,
                    entry.getKey().format(DateTimeFormatter.ISO_LOCAL_DATE),
                    ext
                );
                System.out.println(filename);

                Path path = Paths.get(filename);
                try (PrintWriter writer = new PrintWriter(
                    Files.newBufferedWriter(path, Settings.charset))
                ){
                    writer.println(header);
                    entry.getValue().stream()
                        .forEach(record ->
                            writer.printf(
                                recordFormat,
                                record.getValue() * valueRatio,
                                record.getKey().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                        );
                } catch(IOException exc) {
                    System.out.println(Settings.FILE_WRITE_ERROR);
                }
            });
    }
}