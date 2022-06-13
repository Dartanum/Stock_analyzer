package ru.dartanum.stock_analyzer.app.impl.parser.telegram;

import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import ru.dartanum.stock_analyzer.app.api.analyzer.Analyzer;
import ru.dartanum.stock_analyzer.app.api.parser.telegram.CollectAndAnalyzeDataInbound;
import ru.dartanum.stock_analyzer.app.api.parser.telegram.TelegramParser;
import ru.dartanum.stock_analyzer.app.api.repository.PostRepository;
import ru.dartanum.stock_analyzer.app.api.telegrambot.SendMessageOutbound;
import ru.dartanum.stock_analyzer.domain.Category;
import ru.dartanum.stock_analyzer.domain.Post;
import ru.dartanum.stock_analyzer.framework.spring.ValuesProperties;

import java.util.Set;

import static java.lang.String.format;
import static ru.dartanum.stock_analyzer.domain.Category.UNDEFINED;

@Component
@RequiredArgsConstructor
public class CollectAndAnalyzeDataUseCase implements CollectAndAnalyzeDataInbound {
    private static final String ANALYZE_RESULT_MESSAGE_TEMPLATE = "__Результаты__:\n" +
            "\\- *Каналы*: %s\n" +
            "\\- *Количество постов*: %d\n";
    private static final String RESULT_TEMPLATE =
            "\\- *Прогноз*: %s\n";
    private static final String RESULT_WITH_PROBABILITY_TEMPLATE =
            RESULT_TEMPLATE +
            "\\- *Вероятность*: %d%%";

    private final Analyzer analyzer;
    private final TelegramParser telegramParser;
    private final PostRepository postRepository;
    private final ValuesProperties valuesProperties;
    private final SendMessageOutbound sendMessage;
    private final SendMessageOutbound sendMarkdownMessage;

    @Override
    public void execute() {
        Set<Post> newData = telegramParser.parse();
        if (newData == null) {
            sendMessage.execute("Произошла ошибка во время сбора данных");
            return;
        }
        sendMessage.execute("Сбор данных завершен.\nЗапускаю анализ...");

        Set<Post> analyzedData = analyzer.analyze(newData);
        var verdict = analyzer.getVerdict(analyzedData);

        handleVerdict(verdict, analyzedData.size());
        postRepository.saveAll(analyzedData);
    }

    private void handleVerdict(Pair<Category, Float> verdict, long dataSize) {
        String messageContent = format(ANALYZE_RESULT_MESSAGE_TEMPLATE,
                valuesProperties.getChannels().stream().reduce("", (s, s2) -> s += "\n \\* _" + s2 + "_"),
                dataSize);
        var category = verdict.getFirst();

        if (category == UNDEFINED) {
            messageContent += format(RESULT_TEMPLATE, UNDEFINED.getName());
        } else {
            messageContent += format(RESULT_WITH_PROBABILITY_TEMPLATE, category.getName(), Math.round(verdict.getSecond() * 100f));
        }

        sendMarkdownMessage.execute(messageContent);
    }
}
