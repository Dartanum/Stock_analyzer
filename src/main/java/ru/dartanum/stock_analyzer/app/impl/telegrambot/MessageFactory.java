package ru.dartanum.stock_analyzer.app.impl.telegrambot;

import ru.dartanum.stock_analyzer.domain.news.Category;

import java.util.List;

import static java.lang.String.format;

public class MessageFactory {

    public static String mdNewsAnalysisResult(List<String> channels, long postCount, Category prediction, int probabilityPercent) {
        final String messageTemplate = """
                __Результаты__:
                \\- *Каналы*: %s
                \\- *Количество постов*: %d
                \\- *Прогноз*: %s
                \\- *Вероятность*: %s
                """;

        return format(messageTemplate,
                channels.stream().reduce("", (s, s2) -> s += "\n \\* _" + s2 + "_"),
                postCount,
                prediction.getName(),
                prediction == Category.UNDEFINED ? "\\-" : probabilityPercent + "%"
        );
    }
}
