package ru.dartanum.stock_analyzer.domain.technical;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.IntStream;

@Entity
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class WaveModel {
   @Id
   @GeneratedValue
   private UUID id;

   @ManyToOne
   @JoinColumn(name = "regression_id")
   private RegressionModel regression;
   private LocalDate startDate;
   private LocalDate endDate;
   private double frequency;
   private double amplitude;
   private double phase;
   private double period;
   private double correlation;

   public WaveModel(double frequency, double amplitude, double phase, double period) {
      this.frequency = frequency;
      this.amplitude = amplitude;
      this.phase = phase;
      this.period = period;
   }

   public double[] calcValues(int[] timeSeries) {
       return IntStream.of(timeSeries).mapToDouble(this::calcValue).toArray();
   }

   public double calcValue(int time) {
      return amplitude * Math.cos(2 * Math.PI * time * frequency + phase);
   }
}
