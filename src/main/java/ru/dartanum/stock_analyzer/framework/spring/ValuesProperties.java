package ru.dartanum.stock_analyzer.framework.spring;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "values")
public class ValuesProperties {
    private List<String> channels;
    private Parser parser;
    private Classifier classifier;

    @Getter
    @Setter
    public static class Parser {
        private int minWordsInMessage;
        private List<String> keyWords;
        private NumberOfDays numberOfDaysForCollectData;
    }

    @Getter
    @Setter
    public static class NumberOfDays {
        private int min;
        private int max;
    }

    @Getter
    @Setter
    public static class Classifier {
        private int capacity;
    }
}
