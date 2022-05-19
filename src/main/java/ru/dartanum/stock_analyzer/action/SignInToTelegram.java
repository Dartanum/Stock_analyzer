package ru.dartanum.stock_analyzer.action;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.dartanum.stock_analyzer.parser.TelegramParser;

@Component
@RequiredArgsConstructor
public class SignInToTelegram {
    private final TelegramParser telegramParser;

    public void execute() {
        telegramParser.signIn();
    }
}
