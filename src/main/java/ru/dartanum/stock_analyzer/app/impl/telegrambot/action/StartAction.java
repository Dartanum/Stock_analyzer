package ru.dartanum.stock_analyzer.app.impl.telegrambot.action;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.dartanum.stock_analyzer.app.impl.telegrambot.BotState;
import ru.dartanum.stock_analyzer.app.impl.telegrambot.KeyboardFactory;
import ru.dartanum.stock_analyzer.app.impl.telegrambot.constant.BotReplyConstants;

@Component
public class StartAction implements MessageAction {
    @Override
    public BotState execute(Message message, SendMessage sendMessage) {
        sendMessage.setText(BotReplyConstants.MSG_START);
        sendMessage.setReplyMarkup(KeyboardFactory.menuKeyboard());

        return BotState.MENU;
    }
}
