package ru.dartanum.stock_analyzer.app.impl.telegrambot;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "telegram.bot")
public final class TelegramProps {
    private String token;
    private String username;
}
