package ru.dartanum.stock_analyzer.app.impl.telegrambot.action.newsanalisys;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.dartanum.stock_analyzer.app.impl.telegrambot.BotState;
import ru.dartanum.stock_analyzer.app.impl.telegrambot.action.MessageAction;
import ru.dartanum.stock_analyzer.framework.spring.ValuesProperties;

import static java.lang.String.format;
import static ru.dartanum.stock_analyzer.app.impl.telegrambot.constant.BotReplyConstants.MSG_TEMPLATE_ENTER_DAY_COUNT;

@Component
@RequiredArgsConstructor
public class StartNewsAnalysisConfigurationProcessAction implements MessageAction {
    private final ValuesProperties valuesProperties;

    @Override
    public BotState execute(Message message, SendMessage sendMessage) {
        var minMaxDayCount = valuesProperties.getParser().getNumberOfDaysForCollectData();
        sendMessage.setText(format(MSG_TEMPLATE_ENTER_DAY_COUNT, minMaxDayCount.getMin(), minMaxDayCount.getMax()));
        return BotState.CONFIGURATION_NEWS_ANALYSIS_STARTED;
    }
}
