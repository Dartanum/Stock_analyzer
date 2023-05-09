package ru.dartanum.stock_analyzer.app.impl.analyzer;

import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.dartanum.stock_analyzer.app.api.repository.PostRepository;
import ru.dartanum.stock_analyzer.app.impl.analyzer.news.NewsAnalyzer;
import ru.dartanum.stock_analyzer.app.impl.parser.telegram.TelegramParser;
import ru.dartanum.stock_analyzer.app.impl.telegrambot.Bot;
import ru.dartanum.stock_analyzer.app.impl.telegrambot.MessageFactory;
import ru.dartanum.stock_analyzer.domain.news.Category;
import ru.dartanum.stock_analyzer.domain.news.Post;
import ru.dartanum.stock_analyzer.framework.spring.ValuesProperties;

import java.util.Set;

import static ru.dartanum.stock_analyzer.app.impl.telegrambot.constant.BotReplyConstants.MSG_END_COLLECTING_DATA;
import static ru.dartanum.stock_analyzer.app.impl.telegrambot.constant.BotReplyConstants.MSG_ERROR_DURING_COLLECTING_DATA;

@Component
@RequiredArgsConstructor
public class Analyzer {
    private final NewsAnalyzer newsAnalyzer;
    private final Bot bot;
    private final TelegramParser telegramParser;
    private final PostRepository postRepository;
    private final ValuesProperties valuesProperties;

    public void analyzeNews(Long userId, Long chatId) {
        Set<Post> newData = telegramParser.parse(userId);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);

        if (newData == null) {
            sendMessage.setText(MSG_ERROR_DURING_COLLECTING_DATA);
            bot.send(sendMessage);
            return;
        }
        sendMessage.setText(MSG_END_COLLECTING_DATA);
        bot.send(sendMessage);

        Set<Post> analyzedData = newsAnalyzer.analyze(newData);
        var verdict = newsAnalyzer.getVerdict(analyzedData);


        handleVerdict(verdict, analyzedData.size(), chatId);
        postRepository.saveAll(analyzedData);
    }

    private void handleVerdict(Pair<Category, Float> verdict, long dataSize, long chatId) {
        String text = MessageFactory.mdNewsAnalysisResult(valuesProperties.getChannels(), dataSize, verdict.getFirst(), Math.round(verdict.getSecond() * 100f));
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(text);
        sendMessage.setChatId(chatId);
        sendMessage.enableMarkdownV2(true);

        bot.send(sendMessage);
    }
}
