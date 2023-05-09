package ru.dartanum.stock_analyzer.app.impl.analyzer.technical;

import java.util.Arrays;
import java.util.List;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import static org.apache.commons.lang3.ArrayUtils.toPrimitive;

public class MathUtils {
    public static final double TRUST_COEFFICIENT = 2.0;

    private MathUtils() {}

    public static double standardDeviation(double[] values) {
        var mean = avg(values);

        return Math.sqrt(DoubleStream.of(values)
                .map(val -> Math.pow(val - mean, 2))
                .sum() / values.length);
    }

    public static double estimateCoefficientCorrelation(double[] x, double[] y) {
        int n = x.length;
        double xAvg = avg(x);
        double yAvg = avg(y);

        Double numerator = IntStream.range(0, n)
                .mapToDouble(ind -> (x[ind] - xAvg) * (y[ind] - xAvg))
                .sum();
        Double denominator = Math.sqrt(
                IntStream.range(0, n).mapToDouble(ind -> Math.pow(x[ind] - xAvg, 2)).sum()
                        *
                        IntStream.range(0, n).mapToDouble(ind -> Math.pow(y[ind] - yAvg, 2)).sum()
        );

        return numerator / denominator;
    }

    public static double deviationFromReal(double[] real, double[] regression) {
        double squareErrorSum = IntStream.range(0, real.length).mapToDouble(ind -> Math.pow(real[ind] - regression[ind], 2)).sum();

        return TRUST_COEFFICIENT * Math.sqrt(squareErrorSum / (real.length - 2));
    }

    public static double[] errorFromReal(double[] real, double[] regression) {
        return IntStream.range(0, real.length).mapToDouble(ind -> real[ind] - regression[ind]).toArray();
    }

    public static double[] toPrimitiveArray(List<Double> values) {
        return toPrimitive(values.toArray(Double[]::new));
    }

    public static double avg(double[] values) {
        return DoubleStream.of(values).average().orElse(0.0);
    }

    public static double[] toDoubleArray(int[] array) {
        return Arrays.stream(array).mapToDouble(value -> (double) value).toArray();
    }

    public static double[] derivative1(double[] y, int[] x) {
        double h = x[1] - x[0];

        return IntStream.of(x).mapToDouble(ind -> (y[ind] - (ind == 0 ? 0 : y[ind - 1])) / h).toArray();
    }

    public static double[] derivative2(double[] y, int[] x) {
        return derivative1(y, x);
    }
}
