package ru.dartanum.stock_analyzer.app.impl.telegrambot.action.technical;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.dartanum.stock_analyzer.app.api.repository.ModelRepository;
import ru.dartanum.stock_analyzer.app.impl.analyzer.technical.ModelCreator;
import ru.dartanum.stock_analyzer.app.impl.analyzer.technical.TechnicalAnalyzer;
import ru.dartanum.stock_analyzer.app.impl.telegrambot.Bot;
import ru.dartanum.stock_analyzer.app.impl.telegrambot.BotState;
import ru.dartanum.stock_analyzer.app.impl.telegrambot.action.MessageAction;
import ru.tinkoff.piapi.contract.v1.InstrumentShort;
import ru.tinkoff.piapi.core.InvestApi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static ru.dartanum.stock_analyzer.app.impl.telegrambot.constant.BotReplyConstants.MSG_UNDEFINED_TICKER_NAME;

@Component
@RequiredArgsConstructor
public class EnterTickerNameAction implements MessageAction {
    private final Bot bot;
    private final InvestApi investApi;
    private final ModelRepository modelRepository;
    private final ModelCreator modelCreator;
    private final TechnicalAnalyzer technicalAnalyzer;

    @Transactional
    @Override
    public BotState execute(Message message, SendMessage sendMessage) {
        String tickerName = message.getText().toUpperCase();
        List<InstrumentShort> searchResult = investApi.getInstrumentsService().findInstrument(tickerName).join();

        if (searchResult.isEmpty()) {
            sendMessage.setText(MSG_UNDEFINED_TICKER_NAME);
            return BotState.CONFIGURATION_TECHNICAL_ANALYSIS_STARTED;
        }

        String figi = searchResult.stream()
                .filter(instrument -> instrument.getTicker().equals(tickerName) &&
                        instrument.getInstrumentType().equals("share"))
                .sorted((o1, o2) -> o2.getClassCode().equals("TQBR") ? 1 : o2.getClassCode().equals(o1.getClassCode()) ? 0 : -1)
                .findFirst().get()
                .getFigi();
        var model = modelRepository.findByFigi(figi).orElse(modelCreator.create(figi, tickerName));

        new Thread(() -> {
            ModelCreator.downloadHistoryData(figi, tickerName);
            var result = technicalAnalyzer.analyze(model, tickerName);

            SendDocument sendDocument = new SendDocument();
            sendDocument.setDocument(new InputFile(result.getLeft()));
            sendDocument.setChatId(message.getChatId());
            sendDocument.setCaption("Совет: " + result.getRight().getActionName());
            try {
                bot.execute(sendDocument);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }).start();

        sendMessage.setText("Анализирую...");
        return BotState.MENU;
    }
}
