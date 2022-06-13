package ru.dartanum.stock_analyzer.app.impl.parser.telegram;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.dartanum.stock_analyzer.app.api.parser.telegram.EnterTelegramCodeInbound;
import ru.dartanum.stock_analyzer.app.api.parser.telegram.TelegramParser;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class EnterTelegramCodeUseCase implements EnterTelegramCodeInbound {
    private final TelegramParser telegramParser;

    @Override
    public boolean execute(String message) {
        return telegramParser.enterCode(parseCode(message));
    }

    private String parseCode(String message) {
        return Arrays.stream(message.split("\\.")).reduce("", (s, s2) -> s + s2);
    }
}
