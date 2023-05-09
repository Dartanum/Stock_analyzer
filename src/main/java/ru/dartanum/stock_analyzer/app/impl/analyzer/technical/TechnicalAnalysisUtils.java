package ru.dartanum.stock_analyzer.app.impl.analyzer.technical;

import ru.dartanum.stock_analyzer.domain.technical.RegressionModel;
import ru.dartanum.stock_analyzer.domain.technical.WaveModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import static java.lang.Math.PI;

public class TechnicalAnalysisUtils {
    public static boolean isRegressionValid(double[] y, int[] x, RegressionModel regression) {
        double[] bottomBound = regression.calcBottomBoundValues(x);
        double[] upperBound = regression.calcUpperBoundValues(x);

        return Arrays.stream(x).filter(t -> y[t] > upperBound[t - x[0]] || y[t] < bottomBound[t - x[0]]).count() < (x.length * 0.06);
    }

    // x - time (121, 122, 123)   y - values (index from 0 to time.length)
    public static RegressionModel calculateRegressionModel(int[] x, double[] y) {
        int[] yIndexByTime = IntStream.range(0, x.length).toArray();
        double[] time = MathUtils.toDoubleArray(yIndexByTime);

        var b1 = MathUtils.standardDeviation(y) / MathUtils.standardDeviation(time) * MathUtils.estimateCoefficientCorrelation(time, y);
        var b0 = MathUtils.avg(y) - b1 * MathUtils.avg(MathUtils.toDoubleArray(x));
        var result = new RegressionModel(b0, b1);
        result.setDeviation(MathUtils.deviationFromReal(y, result.calcValues(x)));

        return result;
    }

    public static List<WaveModel> fourierTransform(double[] y, int predictionPeriod) {
        final int L = y.length - 1;
        double[] lastPredictionPeriod = Arrays.copyOfRange(y, y.length - predictionPeriod, y.length);
        int[] timeSeries = IntStream.range(0, y.length).toArray();
        List<WaveModel> result = new ArrayList<>();

        for (int i = 1; i <= y.length / 2; i++) {
            int m = i;
            double a = 2.0 / L * IntStream.range(0, L - 1).mapToDouble(ind -> y[ind] * Math.cos(m * 2 * PI * ind / L)).sum();
            double b = 2.0 / L * IntStream.range(1, L - 1).mapToDouble(ind -> y[ind] * Math.sin(m * 2 * PI * ind / L)).sum();
            double frequency = (double) m / L;

            WaveModel waveModel = new WaveModel(
                    frequency,
                    Math.sqrt(a * a + b * b),
                    -Math.atan(b / a),
                    1 / frequency
            );

            double[] waveModelValues = waveModel.calcValues(timeSeries);
            double[] waveModelLastPredictionPeriod = Arrays.copyOfRange(waveModelValues, waveModelValues.length - predictionPeriod, waveModelValues.length);
            waveModel.setCorrelation(MathUtils.estimateCoefficientCorrelation(lastPredictionPeriod, waveModelLastPredictionPeriod));
            result.add(waveModel);
        }

        return result;
    }

    public static List<WaveModel> getValuableWaveModels(List<WaveModel> waveModels, int predictionPeriod) {
        return waveModels.stream()
                .filter(waveModel -> waveModel.getPeriod() >= predictionPeriod)
                .sorted(Comparator.comparingDouble(WaveModel::getAmplitude).reversed())
                .limit(2)
//                .sorted(Comparator.comparingDouble((WaveModel waveModel) -> waveModel.correlation).reversed())
//                .limit(2)
                .toList();
    }

    public static int findRegressionRebuildTime(double[] y, int[] timeSeries, RegressionModel regression) {
        double[] signal = new double[timeSeries.length];
        int gap = timeSeries[0];
        double[] upperBound = regression.calcUpperBoundValues(timeSeries);
        double[] bottomBound = regression.calcBottomBoundValues(timeSeries);

        Arrays.stream(timeSeries).forEach(t -> {
            if (y[t] > upperBound[t - gap] || y[t] < bottomBound[t - gap]) {
                signal[t - gap] = (t - gap - 1) < 0
                        ? 1
                        : signal[t - gap - 1] + 1;
            } else {
                signal[t - gap] = Math.max((t - gap - 1) < 0
                                ? 0
                                : signal[t - gap - 1] - 1
                        , 0);
            }
        });

        double[] derivative = MathUtils.derivative2(signal, IntStream.range(0, timeSeries.length).toArray());
        int[] periods = IntStream.range(1, signal.length)
                .filter(ind -> derivative[ind - 1] != derivative[ind] && Math.signum(derivative[ind - 1]) != Math.signum(derivative[ind]))
                .toArray();

        return (periods.length == 0
                ? timeSeries[timeSeries.length - 1]
                : timeSeries[periods[periods.length - 1]]);
    }
}
