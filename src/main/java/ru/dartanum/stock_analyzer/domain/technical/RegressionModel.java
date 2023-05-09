package ru.dartanum.stock_analyzer.domain.technical;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class RegressionModel {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "figi")
    private Model model;
    private LocalDate startDate;
    private LocalDate endDate;
    private double b0;
    private double b1;
    private double deviation;

    @OneToMany(mappedBy = "regression", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WaveModel> waveModels = new ArrayList<>();

    public RegressionModel(double b0, double b1) {
        this.b0 = b0;
        this.b1 = b1;
    }

    public double[] calcValues(int[] x) {
        return Arrays.stream(x).mapToDouble(value -> b0 + b1 * value).toArray();
    }

    public double[] calcUpperBoundValues(int[] x) {
        return Arrays.stream(x).mapToDouble(value -> b0 + deviation + b1 * value).toArray();
    }

    public double[] calcBottomBoundValues(int[] x) {
        return Arrays.stream(x).mapToDouble(value -> b0 - deviation + b1 * value).toArray();
    }
}
