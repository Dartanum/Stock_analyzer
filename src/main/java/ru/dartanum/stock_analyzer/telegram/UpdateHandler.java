package ru.dartanum.stock_analyzer.telegram;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Arrays;
import java.util.function.Consumer;

import static ru.dartanum.stock_analyzer.telegram.BotState.*;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class UpdateHandler {
    @Value("${telegram.user.admin-username}")
    private String adminUsername;
    private final SessionStorage sessionStorage;
    private final MessageHandler messageHandler;

    public BotApiMethod<?> handleUpdate(Update update, Consumer<String> sendMessage) {
        if (adminUsername.equals(update.getMessage().getChat().getUserName())) {
            if (update.hasCallbackQuery()) {
                CallbackQuery query = update.getCallbackQuery();
                return null;
            } else {
                Message message = update.getMessage();
                if (message != null && message.hasText()) {
                    return handleInputMessage(message, sendMessage);
                }
            }
        }

        return null;
    }

    private BotApiMethod<?> handleInputMessage(Message message, Consumer<String> sendMessage) {
        String inputMsg = message.getText();
        BotState state = EMPTY;
        BotState lastState = sessionStorage.getBotState();

        if (sessionStorage.getChatId() == null) {
            sessionStorage.setChatId(message.getChatId());
        }
        if (lastState != null) {
            state = lastState;
        }
        switch (inputMsg) {
            case "/start":
                state = START;
                break;
            case "/create_session":
                state = CREATE_SESSION;
                break;
            case "/analyze":
                state = START_ANALYZE;
                break;
            default:
                if (inputMsg.length() == 9 && Arrays.stream(inputMsg.split("\\.")).count() == 5) {
                    state = SEND_CODE;
                }
        }

        return messageHandler.handle(message, state, sendMessage);
    }
}
