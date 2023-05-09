package ru.dartanum.stock_analyzer.app.impl.telegrambot.action.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.dartanum.stock_analyzer.app.impl.parser.telegram.TelegramParser;
import ru.dartanum.stock_analyzer.app.impl.telegrambot.BotState;
import ru.dartanum.stock_analyzer.app.impl.telegrambot.action.MessageAction;

import static ru.dartanum.stock_analyzer.app.impl.telegrambot.constant.BotReplyConstants.MSG_ERROR_DURING_ENTER_VERIFICATION_CODE;
import static ru.dartanum.stock_analyzer.app.impl.telegrambot.constant.BotReplyConstants.MSG_SUCCESS_SYSTEM_SETUP;

@Component
@RequiredArgsConstructor
public class EnterVerificationCodeAction implements MessageAction {
    private final TelegramParser telegramParser;

    @Override
    public BotState execute(Message message, SendMessage sendMessage) {
        String code = message.getText().replace(".", "");
        boolean result = telegramParser.enterCode(code);

        if (result) {
            sendMessage.setText(MSG_SUCCESS_SYSTEM_SETUP);
            return BotState.DEFAULT;
        } else {
            sendMessage.setText(MSG_ERROR_DURING_ENTER_VERIFICATION_CODE);
            return BotState.CODE_SENT;
        }
    }
}
