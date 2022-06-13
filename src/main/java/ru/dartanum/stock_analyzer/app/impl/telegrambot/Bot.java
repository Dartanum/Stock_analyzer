package ru.dartanum.stock_analyzer.app.impl.telegrambot;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@RequiredArgsConstructor
public class Bot extends TelegramWebhookBot {
    private final TelegramProps props;
    private final UpdateHandler updateHandler;

    @Override
    public String getBotToken() {
        return props.getToken();
    }

    @Override
    public String getBotUsername() {
        return props.getUsername();
    }

    @Override
    public String getBotPath() {
        return props.getWebhookPath();
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        return updateHandler.handleUpdate(update);
    }

    void sendApiMethod(SendMessage message) throws TelegramApiException {
        super.sendApiMethod(message);
    }
}
