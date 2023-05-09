package ru.dartanum.stock_analyzer.app.impl.telegrambot.action.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.dartanum.stock_analyzer.app.impl.parser.telegram.TelegramParser;
import ru.dartanum.stock_analyzer.app.impl.telegrambot.BotState;
import ru.dartanum.stock_analyzer.app.impl.telegrambot.SessionStorage;
import ru.dartanum.stock_analyzer.app.impl.telegrambot.action.MessageAction;

import static java.lang.String.format;
import static ru.dartanum.stock_analyzer.app.impl.telegrambot.constant.BotReplyConstants.*;

@Component
@RequiredArgsConstructor
public class SetupParserAction implements MessageAction {
    private final TelegramParser telegramParser;

    @Value("${telegram.admin.phone-number}")
    private String adminPhoneNumber;

    @Override
    public BotState execute(Message message, SendMessage sendMessage) {
        if (!SessionStorage.isAdmin(message.getFrom().getId())) {
            sendMessage.setText(MSG_ACCESS_DENIED);
            return BotState.DEFAULT;
        }

        var result = telegramParser.signIn();

        if (result) {
            sendMessage.setText(format(MSG_TEMPLATE_CODE_SENT, adminPhoneNumber));
            return BotState.CODE_SENT;
        } else {
            sendMessage.setText(MSG_ERROR_DURING_SIGN_IN);
            return BotState.DEFAULT;
        }
    }
}
