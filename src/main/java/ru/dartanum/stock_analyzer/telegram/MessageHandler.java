package ru.dartanum.stock_analyzer.telegram;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.dartanum.stock_analyzer.parser.TelegramParser;

import java.util.Arrays;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class MessageHandler {
    private final TelegramParser telegramParser;

    public BotApiMethod<?> handle(Message message, BotState state, Consumer<String> sendMessageMethod) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(message.getChatId()));
        try {
            switch (state) {
                case EMPTY:
                    sendMessage.setText("Неизвестная команда");
                    break;
                case START:
                    sendMessage.setText("Привет, я создан чтобы управлять анализатором акций. Чтобы начать введи: /create_session");
                    break;
                case CREATE_SESSION:
                    telegramParser.signIn();
                    sendMessage.setText("Сессия создана. Сейчас тебе придет пятизначный код для входа в телеграм. Его необходимо отправить в сообщении формата '1.2.3.4.5'");
                    break;
                case SEND_CODE:
                    telegramParser.enterCode(parseCode(message.getText()));
                    sendMessage.setText("Код введен. Для начала анализа введи: /analyze");
                    break;
                case START_ANALYZE:
                    sendMessageMethod.accept("Собираю данные...");
                    telegramParser.collectData();
                    sendMessage.setText("Сбор данных завершен");
            }
        } catch (Exception e) {
            return null;
        }
        return sendMessage;
    }

    private String parseCode(String message) {
        return Arrays.stream(message.split("\\.")).reduce("", (s, s2) -> s + s2);
    }
}
