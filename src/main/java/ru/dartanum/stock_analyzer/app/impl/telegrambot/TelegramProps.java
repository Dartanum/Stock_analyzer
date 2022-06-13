package ru.dartanum.stock_analyzer.app.impl.telegrambot;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@ConfigurationProperties(prefix = "telegram.bot")
public final class TelegramProps {
    String token;
    String username;
    String webhookPath;
}
