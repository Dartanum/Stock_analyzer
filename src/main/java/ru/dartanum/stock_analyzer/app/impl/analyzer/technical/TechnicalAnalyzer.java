package ru.dartanum.stock_analyzer.app.impl.analyzer.technical;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.math.plot.Plot2DPanel;
import org.springframework.stereotype.Component;
import ru.dartanum.stock_analyzer.domain.technical.Advice;
import ru.dartanum.stock_analyzer.domain.technical.Model;
import ru.dartanum.stock_analyzer.domain.technical.RegressionModel;
import ru.dartanum.stock_analyzer.domain.technical.WaveModel;
import ru.dartanum.stock_analyzer.utils.HistoryDataDownloader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.math.plot.PlotPanel.WEST;

@Component
@RequiredArgsConstructor
public class TechnicalAnalyzer {
    private static final int MIN_PRICE_GAP_PERCENT = 3;

    public Pair<File, Advice> analyze(Model model, String tickerName) {
        var regression = model.getRegressionModels().stream()
                .filter(regressionModel -> regressionModel.getEndDate() == null)
                .findFirst()
                .orElseThrow();
        double[] y;

        try {
            y = HistoryDataDownloader.getHistoryDataValues(HistoryDataReader.readAllByTicker(tickerName), model.getStartDate());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        double[] x = IntStream.range(0, y.length).mapToDouble(value -> (double) value).toArray();
        int[] xWithPrediction = IntStream.range(0, y.length + ModelCreator.PREDICTION_PERIOD).toArray();
        double[] xWithPredictionDouble = Arrays.stream(xWithPrediction).mapToDouble(value -> (double) value).toArray();

        double[] regressionValues = regression.calcValues(xWithPrediction);
        double[] bottomBoundValues = regression.calcBottomBoundValues(xWithPrediction);
        double[] upperBoundValues = regression.calcUpperBoundValues(xWithPrediction);
        double[] wavesSum = wavesSum(regression.getWaveModels(), xWithPrediction);
        double[] fourierValues = sumOf(regressionValues, wavesSum);

        File analysisFile = draw(tickerName, model.getStartDate(), x, y, xWithPredictionDouble, regressionValues, upperBoundValues, bottomBoundValues, fourierValues);
        Advice advice = createAdvice(regression, wavesSum);

        return Pair.of(analysisFile, advice);
    }

    private File draw(String tickerName, LocalDate startDate, double[] x, double[] y, double[] xPrediction, double[] regressionValues,
                      double[] upperBoundValues, double[] bottomBoundValues, double[] fourierValues) {
        Plot2DPanel plot = new Plot2DPanel();
        plot.addScatterPlot(tickerName, Color.BLUE, x, y);
        plot.addLinePlot("Тренд", Color.GREEN, xPrediction, regressionValues);
        plot.addLinePlot("Уровень сопротивления", Color.BLUE, xPrediction, upperBoundValues);
        plot.addLinePlot("Уровень поддержки", Color.BLUE, xPrediction, bottomBoundValues);
        plot.addLinePlot("Приближение Фурье", Color.MAGENTA, xPrediction, fourierValues);
        plot.addLegend(WEST);
        var axisX = plot.getAxis(0);
        axisX.setLabelText("date");
        plot.getAxis(1).setLabelText("price");

        axisX.setLightLabelText(
                IntStream.iterate(0, value -> value < x.length * 2, operand -> operand + 20)
                        .mapToObj(val -> startDate.plusDays(val).toString())
                        .toArray(String[]::new));
        JFrame frame = new JFrame("Корреляция");
        frame.setSize(1920, 1080);
        frame.setContentPane(plot);
        frame.setVisible(true);

        File analysisFile = new File("results.png");
        try {
            plot.toGraphicFile(analysisFile);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));

        return analysisFile;
    }

    private Advice createAdvice(RegressionModel regressionModel, double[] wavesSum) {
        Advice advice;
        double b1 = regressionModel.getB1();
        int startIndex = (int) DAYS.between(regressionModel.getStartDate(), LocalDate.now());

        if (Math.abs(b1) <= 0.05) {
            advice = Advice.HOLD;
        } else if (b1 > 0.05) {
            return gapInPercent(wavesSum[startIndex], indexOfMaxValueFromIndex(wavesSum, startIndex)) > MIN_PRICE_GAP_PERCENT
                    ? Advice.BUY
                    : Advice.HOLD;
        } else {
            return gapInPercent(wavesSum[startIndex], indexOfMinValueFromIndex(wavesSum, startIndex)) > MIN_PRICE_GAP_PERCENT
                    ? Advice.SELL
                    : Advice.HOLD;
        }

        return advice;
    }

    private double[] wavesSum(List<WaveModel> waves, int[] time) {
        double[] sum = new double[time.length];
        var wavesValues = waves.stream().map(wave -> wave.calcValues(time)).toList();

        for (int ind = 0; ind < time.length; ind++) {
            for (var waveValues : wavesValues) {
                sum[ind] += waveValues[ind];
            }
        }

        return sum;
    }

    private double[] sumOf(double[] a, double[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("Недопустипа разная длина массивов");
        }
        double[] sum = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            sum[i] = a[i] + b[i];
        }

        return sum;
    }

    private int indexOfMaxValueFromIndex(double[] values, int startInd) {
        int maxValueInd = startInd;
        for (; startInd < values.length; startInd++) {
            if (values[maxValueInd] <= values[startInd]) {
                maxValueInd = startInd;
            }
        }

        return maxValueInd;
    }

    private int indexOfMinValueFromIndex(double[] values, int startInd) {
        int minValueInd = startInd;
        for (; startInd < values.length; startInd++) {
            if (values[minValueInd] >= values[startInd]) {
                minValueInd = startInd;
            }
        }

        return minValueInd;
    }

    private double gapInPercent(double past, double present) {
        return Math.abs(present - past) / past * 100;
    }
}
