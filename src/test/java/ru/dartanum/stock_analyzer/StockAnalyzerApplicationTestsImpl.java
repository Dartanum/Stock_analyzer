package ru.dartanum.stock_analyzer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.dartanum.stock_analyzer.framework.spring.StockAnalyzerApplication;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@SpringBootTest(classes = StockAnalyzerApplication.class)
class StockAnalyzerApplicationTestsImpl {

    @Test
    void contextLoads() {
        String text = "9 May 2022, 13:33:56";
        LocalDateTime dateTime = LocalDateTime.parse(text, DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm:ss", Locale.ENGLISH));
    }

}
