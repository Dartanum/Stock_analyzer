package ru.dartanum.stock_analyzer.app.impl.telegrambot;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import static java.util.Collections.singletonList;
import static ru.dartanum.stock_analyzer.app.impl.telegrambot.constant.MessageActionConstants.*;

@Component
public class KeyboardFactory {
    public static ReplyKeyboardMarkup menuKeyboard() {
        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add(new KeyboardButton(ACT_START_SENTIMENT_ANALYZE_CONFIG_PROCESS));
        keyboardRow.add(new KeyboardButton(ACT_START_TECHNICAL_ANALYZE));

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setKeyboard(singletonList(keyboardRow));
        replyKeyboardMarkup.setResizeKeyboard(true);

        return replyKeyboardMarkup;
    }

    public static ReplyKeyboardMarkup startProcessKeyboard() {
        KeyboardRow row = new KeyboardRow() {{
            add(new KeyboardButton(ACT_START_PROCESS));
        }};

        return ReplyKeyboardMarkup.builder()
                .keyboard(singletonList(row))
                .resizeKeyboard(true)
                .oneTimeKeyboard(true)
                .build();
    }

    public ReplyKeyboardRemove getRemoveKeyboardObject() {
        return new ReplyKeyboardRemove(true);
    }
}
