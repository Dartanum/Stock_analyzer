package ru.dartanum.stock_analyzer.action;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;
import ru.dartanum.stock_analyzer.analyzer.Analyzer;
import ru.dartanum.stock_analyzer.domain.Post;
import ru.dartanum.stock_analyzer.parser.TelegramParser;
import ru.dartanum.stock_analyzer.repository.PostRepository;

import java.util.Set;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
@Setter
@Getter
public class CollectAndAnalyzeData {
    private final Analyzer analyzer;
    private final TelegramParser telegramParser;
    private final PostRepository postRepository;

    private Consumer<String> botMessageSend;

    public void execute() {
        Set<Post> newData = telegramParser.parse();
        if (newData == null) {
            botMessageSend.accept("Произошла ошибка во время сбора данных");
            return;
        }
        botMessageSend.accept("Сбор данных завершен.\nЗапускаю анализ...");
        postRepository.saveAll(analyzer.analyze(newData));
        botMessageSend.accept("Данные проанализированы");
    }
}
