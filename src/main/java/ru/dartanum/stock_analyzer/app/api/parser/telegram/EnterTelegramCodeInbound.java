package ru.dartanum.stock_analyzer.app.api.parser.telegram;

public interface EnterTelegramCodeInbound {
    boolean execute(String message);
}
