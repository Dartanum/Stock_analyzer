package ru.dartanum.stock_analyzer.app.impl.telegrambot;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class SessionStorage {
    private BotState botState;
    private Long chatId;
    private int numberOfDaysForCollectData;

    public SessionStorage() {
        botState = BotState.DEFAULT;
    }
}
