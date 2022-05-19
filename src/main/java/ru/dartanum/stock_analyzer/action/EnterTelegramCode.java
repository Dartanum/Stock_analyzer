package ru.dartanum.stock_analyzer.action;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.dartanum.stock_analyzer.parser.TelegramParser;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class EnterTelegramCode {
    private final TelegramParser telegramParser;

    public void execute(String message) {
        telegramParser.enterCode(parseCode(message));
    }

    private String parseCode(String message) {
        return Arrays.stream(message.split("\\.")).reduce("", (s, s2) -> s + s2);
    }
}
