package ru.dartanum.stock_analyzer.domain;

public enum Category {
    POSITIVE("позитивный"),
    NEGATIVE("негативный"),
    NEUTRAL("нейтральный"),
    UNDEFINED("неопределенный");

    private String name;

    Category(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
