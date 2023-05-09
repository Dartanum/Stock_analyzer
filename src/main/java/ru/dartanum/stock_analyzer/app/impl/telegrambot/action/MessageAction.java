package ru.dartanum.stock_analyzer.app.impl.telegrambot.action;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.dartanum.stock_analyzer.app.impl.telegrambot.BotState;

public interface MessageAction extends Action {
    BotState execute(Message message, SendMessage sendMessage);
}
