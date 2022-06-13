package ru.dartanum.stock_analyzer.app.impl.telegrambot;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Set.*;

@AllArgsConstructor
@Getter
public enum BotState {
    DEFAULT(),
    START("/start", of(DEFAULT)),
    CREATE_SESSION("Создать сессию", of(START)),
    SEND_CODE(of(CREATE_SESSION)),
    SELECT_NUMBER_OF_DAYS(of(SEND_CODE)),
    START_ANALYZE("Начать анализ", of(SELECT_NUMBER_OF_DAYS), true);

    private String stateMessage;
    private Set<BotState> previousStates;
    private boolean repeatable;

    BotState(Set<BotState> previousStates) {
        this.previousStates = previousStates;
        this.repeatable = false;
    }

    BotState(String stateMessage, Set<BotState> previousStates) {
        this.stateMessage = stateMessage;
        this.previousStates = previousStates;
    }

    BotState() {
        previousStates = Collections.emptySet();
        repeatable = false;
    }

    public boolean isPrevious(BotState state) {
        return state.previousStates.contains(this) || (repeatable && this == state);
    }

    public static BotState getByMessageAndLastState(String name, BotState previous) {
        BotState botState = Stream.of(BotState.values())
                .filter(state -> state.getStateMessage() != null && state.getStateMessage().equals(name))
                .findFirst()
                .orElse(DEFAULT);
        return previous.isPrevious(botState) ? botState : DEFAULT;
    }
}
