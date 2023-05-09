package ru.dartanum.stock_analyzer.app.impl.telegrambot.action.newsanalisys;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.dartanum.stock_analyzer.app.impl.analyzer.Analyzer;
import ru.dartanum.stock_analyzer.app.impl.telegrambot.BotState;
import ru.dartanum.stock_analyzer.app.impl.telegrambot.action.MessageAction;

import static ru.dartanum.stock_analyzer.app.impl.telegrambot.constant.BotReplyConstants.MSG_START_COLLECTING_DATA;

@Component
@RequiredArgsConstructor
public class StartNewsAnalysisAction implements MessageAction {
    private final Analyzer analyzer;

    @Override
    public BotState execute(Message message, SendMessage sendMessage) {
        new Thread(() -> analyzer.analyzeNews(message.getFrom().getId(), message.getChatId())).start();
        sendMessage.setText(MSG_START_COLLECTING_DATA);

        return BotState.DEFAULT;
    }
}
