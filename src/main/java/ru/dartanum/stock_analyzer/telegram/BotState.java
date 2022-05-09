package ru.dartanum.stock_analyzer.telegram;

public enum BotState {
    EMPTY,
    START,
    CREATE_SESSION,
    SEND_CODE,
    START_ANALYZE
}
