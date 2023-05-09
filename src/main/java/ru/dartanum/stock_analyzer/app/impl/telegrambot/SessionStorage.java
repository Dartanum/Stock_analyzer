package ru.dartanum.stock_analyzer.app.impl.telegrambot;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class SessionStorage {
    private static final Map<Long, UserStorage> storage = new HashMap<>();
    private static long adminId;

    public SessionStorage(@Value("${telegram.admin.id}") long adminId) {
        SessionStorage.adminId = adminId;
    }

    public static boolean isAdmin(Long userId) {
        return userId == adminId;
    }

    public static UserStorage getByUserIdOrSave(Long userId) {
        var userStorage = storage.get(userId);
        if (userStorage == null) {
            userStorage = new UserStorage();
            storage.put(userId, userStorage);
        }
        return userStorage;
    }

    @Setter
    @Getter
    public static class UserStorage {
        private BotState state = BotState.DEFAULT;
        private int numberOfDaysForNewParsing;
    }
}
