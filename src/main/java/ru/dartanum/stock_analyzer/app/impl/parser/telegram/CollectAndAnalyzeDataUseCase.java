package ru.dartanum.stock_analyzer.app.impl.parser.telegram;

import lombok.RequiredArgsConstructor;
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

@Component
@RequiredArgsConstructor
public class CollectAndAnalyzeDataUseCase implements CollectAndAnalyzeDataInbound {
    private static final String ANALYZE_RESULT_MESSAGE_TEMPLATE = "__Результаты__:\n" +
            "\\- *Каналы*: %s\n" +
            "\\- *Количество постов*: %d\n" +
            "\\- *Вердикт*: %s\n" +
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
        postRepository.saveAll(analyzedData);

        sendMarkdownMessage.execute(format(ANALYZE_RESULT_MESSAGE_TEMPLATE,
                valuesProperties.getChannels().stream().reduce("", (s, s2) -> s += "\n \\* _" + s2 + "_"),
                analyzedData.size(),
                verdict.getFirst(),
                (int) (verdict.getSecond() * 100)));
    }
}
