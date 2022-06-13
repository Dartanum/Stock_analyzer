package ru.dartanum.stock_analyzer.app.impl.telegrambot;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Arrays;

import static ru.dartanum.stock_analyzer.app.impl.telegrambot.BotState.*;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class UpdateHandler {
    public static final String UNKNOWN_COMMAND = "Неизвестная команда";
    private final SessionStorage sessionStorage;
    private final MessageHandler messageHandler;

    @Value("${telegram.user.admin-username}")
    private String adminUsername;

    public BotApiMethod<?> handleUpdate(Update update) {
        if (adminUsername.equals(update.getMessage().getChat().getUserName())) {
            if (update.hasCallbackQuery()) {
                CallbackQuery query = update.getCallbackQuery();
                return null;
            } else {
                Message message = update.getMessage();
                if (message != null && message.hasText()) {
                    return handleInputMessage(message);
                }
            }
        }
        return null;
    }

    private BotApiMethod<?> handleInputMessage(Message message) {
        String inputMsg = message.getText();
        BotState state;
        BotState lastState = sessionStorage.getBotState();

        if (sessionStorage.getChatId() == null) {
            sessionStorage.setChatId(message.getChatId());
        }

        if (lastState.isPrevious(SELECT_NUMBER_OF_DAYS) && Integer.parseInt(inputMsg) > 0) {
            state = SELECT_NUMBER_OF_DAYS;
        } else if (lastState.isPrevious(SEND_CODE) && inputMsg.length() == 9 && Arrays.stream(inputMsg.split("\\.")).count() == 5) {
            state = SEND_CODE;
        } else {
            state = getByMessageAndLastState(inputMsg, lastState);
        }

        SendMessage result =  messageHandler.handle(message, state);

        if (!result.getText().equals(UNKNOWN_COMMAND) && state != DEFAULT) {
            sessionStorage.setBotState(state);
        }
        return result;
    }
}
