package ru.dartanum.stock_analyzer.app.impl.telegrambot.action.technical;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.dartanum.stock_analyzer.app.impl.telegrambot.BotState;
import ru.dartanum.stock_analyzer.app.impl.telegrambot.action.MessageAction;

import static ru.dartanum.stock_analyzer.app.impl.telegrambot.constant.BotReplyConstants.MSG_ENTER_TICKER_NAME;

@Component
public class StartTechnicalAnalysisConfigurationProcessAction implements MessageAction {
    @Override
    public BotState execute(Message message, SendMessage sendMessage) {
        sendMessage.setText(MSG_ENTER_TICKER_NAME);
        return BotState.CONFIGURATION_TECHNICAL_ANALYSIS_STARTED;
    }
}
