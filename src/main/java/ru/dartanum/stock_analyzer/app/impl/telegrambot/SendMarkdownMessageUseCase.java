package ru.dartanum.stock_analyzer.app.impl.telegrambot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.dartanum.stock_analyzer.app.api.telegrambot.SendMessageOutbound;

@Component("sendMarkdownMessage")
public class SendMarkdownMessageUseCase implements SendMessageOutbound {
    private final Bot bot;
    private final SessionStorage sessionStorage;

    @Autowired
    public SendMarkdownMessageUseCase(@Lazy Bot bot, SessionStorage sessionStorage) {
        this.bot = bot;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void execute(String message) {
        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(String.valueOf(sessionStorage.getChatId()));
            sendMessage.setText(message);
            sendMessage.enableMarkdownV2(true);

            bot.sendApiMethod(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
