package ru.dartanum.stock_analyzer.app.impl.telegrambot;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.dartanum.stock_analyzer.app.api.parser.telegram.CollectAndAnalyzeDataInbound;
import ru.dartanum.stock_analyzer.app.api.parser.telegram.EnterTelegramCodeInbound;
import ru.dartanum.stock_analyzer.app.api.parser.telegram.SignInToTelegramInbound;
import ru.dartanum.stock_analyzer.framework.spring.ValuesProperties;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static ru.dartanum.stock_analyzer.app.impl.telegrambot.BotState.CREATE_SESSION;
import static ru.dartanum.stock_analyzer.app.impl.telegrambot.BotState.START_ANALYZE;
import static ru.dartanum.stock_analyzer.app.impl.telegrambot.UpdateHandler.UNKNOWN_COMMAND;

@Component
@RequiredArgsConstructor
public class MessageHandler {
    private final CollectAndAnalyzeDataInbound collectAndAnalyzeDataInbound;
    private final SignInToTelegramInbound signInToTelegramInbound;
    private final EnterTelegramCodeInbound enterTelegramCodeInbound;
    private final KeyboardMaker keyboardMaker;
    private final SessionStorage sessionStorage;
    private final ValuesProperties valuesProperties;

    public SendMessage handle(Message message, BotState state) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(message.getChatId()));
        ValuesProperties.NumberOfDays dayBounds = valuesProperties.getParser().getNumberOfDaysForCollectData();

        switch (state) {
            case START:
                handleStart(sendMessage);
                break;
            case CREATE_SESSION:
                handleCreateSession(sendMessage);
                break;
            case SEND_CODE:
                handleSendCode(sendMessage, message, dayBounds.getMin(), dayBounds.getMax());
                break;
            case SELECT_NUMBER_OF_DAYS:
                handleSelectNumberOfDays(sendMessage, message, dayBounds.getMin(), dayBounds.getMax());
                break;
            case START_ANALYZE:
                handleStartAnalyze(sendMessage);
                break;
            default:
                sendMessage.setText(UNKNOWN_COMMAND);
        }

        return sendMessage;
    }

    private void handleStart(SendMessage sendMessage) {
        sendMessage.setText("Привет, я создан чтобы управлять анализатором акций");
        sendMessage.setReplyMarkup(keyboardMaker.getKeyboard(CREATE_SESSION));
    }

    private void handleCreateSession(SendMessage sendMessage) {
        signInToTelegramInbound.execute();
        sendMessage.setText("Сессия создана. Сейчас тебе придет пятизначный код для входа в телеграм. Его необходимо отправить в сообщении формата '1.2.3.4.5'");
        sendMessage.setReplyMarkup(keyboardMaker.getRemoveKeyboardObject());
    }

    private void handleSendCode(SendMessage sendMessage, Message message, int min, int max) {
        String success = format("Код успешно введен\nВведи за сколько дней проанализировать записи (от %d до %d)", min, max);
        String error = "Произошла ошибка, попробуйте снова";

        boolean executionResult = enterTelegramCodeInbound.execute(message.getText());
        sendMessage.setText(executionResult ? success : error);
    }

    private void handleSelectNumberOfDays(SendMessage sendMessage, Message message, int min, int max) {
        int numberOfDays = parseInt(message.getText());

        if (numberOfDays >= min && numberOfDays <= max) {
            sessionStorage.setNumberOfDaysForCollectData(numberOfDays);
            sendMessage.setText("Система сконфигурирована");
            sendMessage.setReplyMarkup(keyboardMaker.getKeyboard(START_ANALYZE));
        } else {
            sendMessage.setText(format("Введенное количество дней должно быть от %d до %d", min, max));
        }
    }

    private void handleStartAnalyze(SendMessage sendMessage) {
        new Thread(collectAndAnalyzeDataInbound::execute).start();
        sendMessage.setText("Собираю данные...");
    }
}
