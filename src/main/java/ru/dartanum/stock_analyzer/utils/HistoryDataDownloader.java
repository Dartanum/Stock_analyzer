package ru.dartanum.stock_analyzer.utils;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import ru.dartanum.stock_analyzer.domain.technical.HistoryData;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static java.time.temporal.ChronoUnit.DAYS;

public class HistoryDataDownloader {
    public static final String HISTORY_DATA_PATH = "src/main/resources/history/";
    private static final String API_PATH = "https://invest-public-api.tinkoff.ru/history-data";

    public static void main(String[] args){
        var figi = args[0];
        var year = args[1];
        var filePrefix = args[2];

        downloadForYear(figi, filePrefix, year);
        Thread.currentThread().interrupt();
    }

    public static double[] getHistoryDataValues(Map<LocalDate, List<HistoryData>> historyDataByDate, LocalDate startDate) {
        List<Pair<LocalDate, Double>> data = historyDataByDate
                .entrySet().stream()
                .filter(entry -> !entry.getKey().isBefore(startDate))
                .map(entry -> Pair.of(entry.getKey(), entry.getValue().get(entry.getValue().size() - 1).getClose()))
                .sorted(Map.Entry.comparingByKey())
                .toList();

        LocalDate now = LocalDate.now();
        double[] result = new double[(int) DAYS.between(startDate, now)];
        AtomicInteger resultInd = new AtomicInteger(0);

        IntStream.range(0, result.length).mapToObj(startDate::plusDays).forEach(date -> {
            List<HistoryData> historyData = historyDataByDate.get(date);
            if (historyData == null) {
                if (resultInd.get() == 0) {
                    result[resultInd.getAndIncrement()] = 0.0;
                } else {
                    result[resultInd.getAndIncrement()] = result[resultInd.get() - 2];
                }
            } else {
                result[resultInd.getAndIncrement()] = historyData.get(historyData.size() - 1).getClose();
            }
        });

        return result;
    }

    public static void downloadForYear(String figi, String filePrefix, String year) {
        try {
            String token = (String) new YamlPropertySourceLoader().load("props", new ClassPathResource("application.yaml")).get(0).getProperty("tinkoff.api.sandbox.token");

            var rest = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + token);
            var response = rest.exchange(
                    format(API_PATH + "?figi=%s&year=%s", figi, year),
                    HttpMethod.GET,
                    new HttpEntity<>("parameters", headers),
                    byte[].class
            );


            if (response.getStatusCode().is2xxSuccessful()) {
                var filePath = Path.of(HISTORY_DATA_PATH + filePrefix.toLowerCase() + "/" + filePrefix + year + ".zip");
                if (!Files.exists(filePath)) {
                    Files.createFile(filePath);
                }

                Files.write(filePath, response.getBody());
            } else {
                System.out.println("Error while attempt to download history data");
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
