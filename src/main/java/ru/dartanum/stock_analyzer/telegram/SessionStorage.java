package ru.dartanum.stock_analyzer.telegram;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class SessionStorage {
    private BotState botState;
    private Long chatId;
}
