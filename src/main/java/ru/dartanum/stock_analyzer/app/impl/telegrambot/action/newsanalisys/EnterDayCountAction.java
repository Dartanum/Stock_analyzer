package ru.dartanum.stock_analyzer.app.impl.telegrambot.action.newsanalisys;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.dartanum.stock_analyzer.app.impl.telegrambot.BotState;
import ru.dartanum.stock_analyzer.app.impl.telegrambot.KeyboardFactory;
import ru.dartanum.stock_analyzer.app.impl.telegrambot.SessionStorage;
import ru.dartanum.stock_analyzer.app.impl.telegrambot.action.MessageAction;

import static ru.dartanum.stock_analyzer.app.impl.telegrambot.constant.BotReplyConstants.MSG_DAY_COUNT_SAVED;

@Component
public class EnterDayCountAction implements MessageAction {
    @Override
    public BotState execute(Message message, SendMessage sendMessage) {
        SessionStorage.getByUserIdOrSave(message.getFrom().getId())
                .setNumberOfDaysForNewParsing(Integer.parseInt(message.getText()));

        sendMessage.setText(MSG_DAY_COUNT_SAVED);
        sendMessage.setReplyMarkup(KeyboardFactory.startProcessKeyboard());

        return BotState.DAY_COUNT_SELECTED;
    }
}
