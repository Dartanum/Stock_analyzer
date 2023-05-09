package ru.dartanum.stock_analyzer.app.impl.telegrambot;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static ru.dartanum.stock_analyzer.app.impl.telegrambot.constant.MessageActionConstants.ACT_START;

@Component
public class UpdateHandler {
    public void handle(Update update, SendMessage sendMessage) {
        if (update.hasCallbackQuery()) {
            var callback = update.getCallbackQuery();
            //handleCallbackQuery(callback, sendMessage);
        } else if (update.hasMessage()) {
            Message message = update.getMessage();
            if (message != null && message.hasText()) {
                handleMessage(message, sendMessage);
            }
        }
    }

    private void handleMessage(Message message, SendMessage sendMessage) {
        long userId = message.getFrom().getId();
        var storage = SessionStorage.getByUserIdOrSave(userId);
        BotState lastState = storage.getState();
        BotState newState;

        if (isStartAction(message)) {
            newState = BotState.MENU;
        } else if (BotState.isGlobalAction(message)) {
            newState = BotState.nextGlobalActionState(sendMessage, message);
        } else {
            newState = lastState.nextState(sendMessage, message);
        }

        if (newState == BotState.MENU) {
            handleMenuState(sendMessage);
        }
        storage.setState(newState);
    }

    private boolean isStartAction(Message message) {
        return message.getText().equals(ACT_START);
    }

    private void handleMenuState(SendMessage sendMessage) {
        if (isBlank(sendMessage.getText())) {
            sendMessage.setText("Выберите действие");
        }
        sendMessage.setReplyMarkup(KeyboardFactory.menuKeyboard());
    }
}
