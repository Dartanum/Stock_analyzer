package ru.dartanum.stock_analyzer.telegram;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.dartanum.stock_analyzer.action.CollectAndAnalyzeData;
import ru.dartanum.stock_analyzer.action.EnterTelegramCode;
import ru.dartanum.stock_analyzer.action.SignInToTelegram;

import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class MessageHandler {
    private final CollectAndAnalyzeData collectAndAnalyzeData;
    private final SignInToTelegram signInToTelegram;
    private final EnterTelegramCode enterTelegramCode;

    public BotApiMethod<?> handle(Message message, BotState state, Consumer<String> sendMessageMethod) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(message.getChatId()));

        if (collectAndAnalyzeData.getBotMessageSend() == null) {
            collectAndAnalyzeData.setBotMessageSend(sendMessageMethod);
        }

        switch (state) {
            case EMPTY:
                sendMessage.setText("Неизвестная команда");
                break;
            case START:
                sendMessage.setText("Привет, я создан чтобы управлять анализатором акций. Чтобы начать введи: /create_session");
                break;
            case CREATE_SESSION:
                signInToTelegram.execute();
                sendMessage.setText("Сессия создана. Сейчас тебе придет пятизначный код для входа в телеграм. Его необходимо отправить в сообщении формата '1.2.3.4.5'");
                break;
            case SEND_CODE:
                enterTelegramCode.execute(message.getText());
                sendMessage.setText("Код введен. Для начала анализа введи: /analyze");
                break;
            case START_ANALYZE:
                Thread analyzeThread = new Thread(collectAndAnalyzeData::execute);
                analyzeThread.start();
                sendMessage.setText("Собираю данные...");
        }

        return sendMessage;
    }
}
