package ru.dartanum.stock_analyzer.app.api.parser.telegram;

import ru.dartanum.stock_analyzer.app.api.parser.Parser;

public interface TelegramParser extends Parser {
    void signIn();

    boolean enterCode(String code);
}
