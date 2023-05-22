package ru.dartanum.stock_analyzer.app.impl.analyzer.technical;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.dartanum.stock_analyzer.app.api.repository.ModelRepository;
import ru.dartanum.stock_analyzer.domain.technical.HistoryData;
import ru.dartanum.stock_analyzer.domain.technical.Model;
import ru.dartanum.stock_analyzer.domain.technical.WaveModel;
import ru.dartanum.stock_analyzer.utils.HistoryDataDownloader;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.IntStream;

import static ru.dartanum.stock_analyzer.app.impl.analyzer.technical.TechnicalAnalysisUtils.*;
import static ru.dartanum.stock_analyzer.utils.HistoryDataDownloader.getHistoryDataValues;

@Component
@RequiredArgsConstructor
public class ModelCreator {
    public static final int PREDICTION_PERIOD = 14;
    private static final int LEARNING_PERIOD = 180;

    private final ModelRepository modelRepository;

    @Transactional
    public Model create(String figi, String tickerName) {
        String[] filePaths = downloadHistoryData(figi, tickerName);
        LocalDate startDate = LocalDate.now().minusDays(LEARNING_PERIOD);

        Map<LocalDate, List<HistoryData>> historyDataByDate = new HashMap<>();
        try {
            historyDataByDate = HistoryDataReader.readAll(filePaths);
        } catch (IOException e) {
            e.printStackTrace();
        }

        double[] prices = getHistoryDataValues(historyDataByDate, startDate);

        int[] t = IntStream.range(0, prices.length).toArray();
        var linearRegression = calculateRegressionModel(t, prices);
        linearRegression.setStartDate(startDate);
        double[] linearRegressionValues = linearRegression.calcValues(t);
        double[] regressionErrors = MathUtils.errorFromReal(prices, linearRegressionValues);
        List<WaveModel> waves = fourierTransform(regressionErrors, PREDICTION_PERIOD);
        List<WaveModel> valuableWaves = getValuableWaveModels(waves, PREDICTION_PERIOD);
        valuableWaves.forEach(wave -> wave.setStartDate(startDate));

        linearRegression.getWaveModels().addAll(valuableWaves);

        return modelRepository.save(new Model(figi, startDate, false, new ArrayList<>(){{add(linearRegression);}}));
    }

    public static String[] downloadHistoryData(String figi, String tickerName) {
        int currentYear = LocalDate.now().getYear();
        HistoryDataDownloader.downloadForYear(figi, tickerName, String.valueOf(currentYear));
        String filePathForCurrentYear = HistoryDataDownloader.HISTORY_DATA_PATH + tickerName + (currentYear) + ".zip";

        if (LocalDate.now().getDayOfYear() < LEARNING_PERIOD) {
            HistoryDataDownloader.downloadForYear(figi, tickerName, String.valueOf(currentYear - 1));
            return new String[]{
                    filePathForCurrentYear,
                    HistoryDataDownloader.HISTORY_DATA_PATH + tickerName + (currentYear - 1) + ".zip",
            };
        } else {
            return new String[]{filePathForCurrentYear};
        }
    }
}
