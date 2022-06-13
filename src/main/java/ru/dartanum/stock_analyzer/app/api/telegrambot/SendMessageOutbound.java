package ru.dartanum.stock_analyzer.app.api.telegrambot;

public interface SendMessageOutbound {
    void execute(String message);
}
