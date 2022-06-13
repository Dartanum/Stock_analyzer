package ru.dartanum.stock_analyzer.app.impl.telegrambot;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import static java.util.Collections.singletonList;
import static ru.dartanum.stock_analyzer.app.impl.telegrambot.BotState.CREATE_SESSION;

@Component
public class KeyboardMaker {

    public ReplyKeyboardMarkup getKeyboard(BotState state) {
        KeyboardRow keyboardRow = new KeyboardRow(1);
        keyboardRow.add(new KeyboardButton(state.getStateMessage()));

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setKeyboard(singletonList(keyboardRow));
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);

        return replyKeyboardMarkup;
    }

    public ReplyKeyboardRemove getRemoveKeyboardObject() {
        return new ReplyKeyboardRemove(true);
    }
}
