package ru.dartanum.stock_analyzer;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@SpringBootTest(classes = StockAnalyzerApplication.class)
class StockAnalyzerApplicationTests {

    @Test
    void contextLoads() {
        String text = "9 May 2022, 13:33:56";
        LocalDateTime dateTime = LocalDateTime.parse(text, DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm:ss", Locale.ENGLISH));
    }

}
