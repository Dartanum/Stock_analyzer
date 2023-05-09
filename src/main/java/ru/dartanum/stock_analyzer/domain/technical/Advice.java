package ru.dartanum.stock_analyzer.domain.technical;

import lombok.Getter;

@Getter
public enum Advice {
    BUY("покупать"), SELL("продавать"), HOLD("держать");

    private final String actionName;

    Advice(String actionName) {
        this.actionName = actionName;
    }
}
