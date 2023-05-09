package ru.dartanum.stock_analyzer.domain.technical;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class HistoryData {
    private String figi;
    private LocalDateTime time;
    private Double open;
    private Double close;
    private Double high;
    private Double low;
    private Integer volume;
}
