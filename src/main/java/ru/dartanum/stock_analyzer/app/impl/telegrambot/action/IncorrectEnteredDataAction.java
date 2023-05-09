package ru.dartanum.stock_analyzer.app.impl.telegrambot.action;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.dartanum.stock_analyzer.app.impl.telegrambot.BotState;
import ru.dartanum.stock_analyzer.app.impl.telegrambot.SessionStorage;

import static ru.dartanum.stock_analyzer.app.impl.telegrambot.constant.BotReplyConstants.MSG_INCORRECT_DATA;

@Component
public class IncorrectEnteredDataAction implements MessageAction {
    @Override
    public BotState execute(Message message, SendMessage sendMessage) {
        sendMessage.setText(MSG_INCORRECT_DATA);
        return SessionStorage.getByUserIdOrSave(message.getFrom().getId()).getState();
    }
}
