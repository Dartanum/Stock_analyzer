package ru.dartanum.stock_analyzer.domain.technical;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Model {
    @Id
    private String figi;
    private LocalDate startDate;
    private boolean isPause;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "model")
    private List<RegressionModel> regressionModels;

}
