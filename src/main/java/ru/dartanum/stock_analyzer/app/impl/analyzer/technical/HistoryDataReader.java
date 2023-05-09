package ru.dartanum.stock_analyzer.app.impl.analyzer.technical;

import ru.dartanum.stock_analyzer.domain.technical.HistoryData;
import ru.dartanum.stock_analyzer.utils.HistoryDataDownloader;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

import static java.lang.String.format;

public class HistoryDataReader {
    private HistoryDataReader() {
    }

    public static Map<LocalDate, List<HistoryData>> readAllByTicker(String ticker) throws IOException {
        File dir = new File(HistoryDataDownloader.HISTORY_DATA_PATH);
        File[] files = dir.listFiles((dir1, name) -> name.startsWith(ticker) && name.endsWith(".zip"));

        if (files == null || files.length == 0) {
            throw new IllegalArgumentException(format("Не найдены исторические данные для тикера '%s'", ticker));
        }

        return readAll(Arrays.stream(files).map(File::getPath).toArray(String[]::new));
    }

    public static Map<LocalDate, List<HistoryData>> readAll(String... zipFilePaths) throws IOException {
        Map<LocalDate, List<HistoryData>> result = new HashMap<>();
        for (String path : zipFilePaths) {
            result.putAll(read(path));
        }

        return result;
    }

    public static Map<LocalDate, List<HistoryData>> read(String zipFilePath) throws IOException {
        Map<LocalDate, List<HistoryData>> result = new HashMap<>();
        ZipFile historyDataZip = new ZipFile(zipFilePath);
        var files = historyDataZip.entries();

        while (files.hasMoreElements()) {
            List<HistoryData> dataByDay = new ArrayList<>();
            var zipEntry = files.nextElement();
            var fileData = historyDataZip.getInputStream(zipEntry).readAllBytes();
            var lines = new String(fileData, StandardCharsets.UTF_8).split("\n");
            Arrays.stream(lines).forEach(line -> {
                var lineData = line.split(";");
                dataByDay.add(new HistoryData(
                        lineData[0],
                        LocalDateTime.ofInstant(Instant.parse(lineData[1]), ZoneId.of("Z")),
                        Double.parseDouble(lineData[2]),
                        Double.parseDouble(lineData[3]),
                        Double.parseDouble(lineData[4]),
                        Double.parseDouble(lineData[5]),
                        Integer.parseInt(lineData[6]))
                );
            });
            Pattern pattern = Pattern.compile(".*?_(\\d{8})\\..*?");
            Matcher matcher = pattern.matcher(zipEntry.getName());
            if (matcher.find()) {
                result.put(LocalDate.parse(matcher.group(1), DateTimeFormatter.ofPattern("yyyyMMdd")), dataByDay);
            }
        }

        return result;
    }
}
