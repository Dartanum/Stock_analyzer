package ru.dartanum.stock_analyzer.app.impl.telegrambot;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Component
@Slf4j
public class Bot extends TelegramLongPollingBot {
    private final TelegramProps props;
    private final UpdateHandler updateHandler;

    public Bot(TelegramProps props, UpdateHandler updateHandler) {
        super(props.getToken());
        this.props = props;
        this.updateHandler = updateHandler;
    }

    @Override
    public String getBotUsername() {
        return props.getUsername();
    }

    @Override
    public void onUpdateReceived(Update update) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.hasCallbackQuery()
                ? update.getCallbackQuery().getMessage().getChatId()
                : update.getMessage().getChatId());
        updateHandler.handle(update, sendMessage);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void send(BotApiMethod method) {
        try {
            execute(method);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @PostConstruct
    public void register() {
        try {
            new TelegramBotsApi(DefaultBotSession.class).registerBot(this);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            log.error("Error during register bot");
        }
    }
}
