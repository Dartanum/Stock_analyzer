package ru.dartanum.stock_analyzer.app.impl.telegrambot;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.dartanum.stock_analyzer.app.impl.telegrambot.action.*;
import ru.dartanum.stock_analyzer.app.impl.telegrambot.action.admin.EnterVerificationCodeAction;
import ru.dartanum.stock_analyzer.app.impl.telegrambot.action.admin.SetupParserAction;
import ru.dartanum.stock_analyzer.app.impl.telegrambot.action.newsanalisys.EnterDayCountAction;
import ru.dartanum.stock_analyzer.app.impl.telegrambot.action.newsanalisys.StartNewsAnalysisAction;
import ru.dartanum.stock_analyzer.app.impl.telegrambot.action.newsanalisys.StartNewsAnalysisConfigurationProcessAction;
import ru.dartanum.stock_analyzer.app.impl.telegrambot.action.technical.EnterTickerNameAction;
import ru.dartanum.stock_analyzer.app.impl.telegrambot.action.technical.StartTechnicalAnalysisConfigurationProcessAction;

import java.util.EnumSet;

import static ru.dartanum.stock_analyzer.app.impl.telegrambot.constant.MessageActionConstants.*;
import static ru.dartanum.stock_analyzer.app.impl.telegrambot.constant.RegularExpressions.VERIFICATION_CODE_REGEXP;

@Getter
public enum BotState {
    GLOBAL_QUERY {
        @Override
        Action selectAction(String text) {
            var context = getApplicationContext();
            return switch (text) {
                case ACT_SETUP_PARSER -> context.getBean(SetupParserAction.class);
                default -> null;
            };
        }
    },

    DEFAULT {
        @Override
        Action selectAction(String text) {
            return ACT_START.equals(text)
                    ? getApplicationContext().getBean(StartAction.class)
                    : null;
        }
    },

    MENU {
        @Override
        Action selectAction(String text) {
            var context = getApplicationContext();
            return switch (text) {
                case ACT_START_SENTIMENT_ANALYZE_CONFIG_PROCESS -> context.getBean(StartNewsAnalysisConfigurationProcessAction.class);
                case ACT_START_TECHNICAL_ANALYZE -> context.getBean(StartTechnicalAnalysisConfigurationProcessAction.class);
                default -> null;
            };
        }
    },
    //---------------------------ADMIN---------------------------
    CODE_SENT {
        @Override
        Action selectAction(String text) {
            return text.matches(VERIFICATION_CODE_REGEXP)
                    ? getApplicationContext().getBean(EnterVerificationCodeAction.class)
                    : null;
        }
    },
    //-----------------------NEWS ANALYSIS-----------------------
    CONFIGURATION_NEWS_ANALYSIS_STARTED {
        @Override
        Action selectAction(String text) {
            try {
                Integer.parseInt(text);
                return getApplicationContext().getBean(EnterDayCountAction.class);
            } catch (NumberFormatException e) {
                return null;
            }
        }
    },

    DAY_COUNT_SELECTED {
        @Override
        Action selectAction(String text) {
            return text.equals(ACT_START_PROCESS)
                    ? getApplicationContext().getBean(StartNewsAnalysisAction.class)
                    : null;
        }
    },
    //---------------------TECHNICAL ANALYSIS_---------------------
    CONFIGURATION_TECHNICAL_ANALYSIS_STARTED {
        @Override
        Action selectAction(String text) {
            return !text.contains(" ")
                    ? getApplicationContext().getBean(EnterTickerNameAction.class)
                    : null;
        }
    },

    TICKER_CHOSEN {
        @Override
        Action selectAction(String text) {
            return null;
        }
    };

    private ApplicationContext applicationContext;

    public BotState nextState(SendMessage sendMessage, Message message) {
        MessageAction action = (MessageAction) selectAction(message.getText());

        return action != null
                ? action.execute(message, sendMessage)
                : getApplicationContext().getBean(IncorrectEnteredDataAction.class).execute(message, sendMessage);
    }

    public static boolean isGlobalAction(Message message) {
        return GLOBAL_QUERY.selectAction(message.getText()) != null;
    }

    public static BotState nextGlobalActionState(SendMessage sendMessage, Message message) {
        MessageAction action = (MessageAction) GLOBAL_QUERY.selectAction(message.getText());

        return action.execute(message, sendMessage);
    }

    abstract Action selectAction(String text);

    public void setContext(ApplicationContext context) {
        applicationContext = context;
    }

    @Component
    public static class ApplicationContextInjector {
        @Autowired
        private ApplicationContext applicationContext;

        @PostConstruct
        public void inject() {
            for (BotState state : EnumSet.allOf(BotState.class)) {
                state.setContext(applicationContext);
            }
        }
    }
}
