package ru.dartanum.stock_analyzer.app.impl.parser.telegram;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.dartanum.stock_analyzer.app.api.parser.telegram.SignInToTelegramInbound;
import ru.dartanum.stock_analyzer.app.api.parser.telegram.TelegramParser;

@Component
@RequiredArgsConstructor
public class SignInToTelegramUseCase implements SignInToTelegramInbound {
    private final TelegramParser telegramParser;

    @Override
    public void execute() {
        telegramParser.signIn();
    }
}
