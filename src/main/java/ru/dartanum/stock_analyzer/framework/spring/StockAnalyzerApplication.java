package ru.dartanum.stock_analyzer.framework.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = "ru.dartanum.stock_analyzer")
@EnableConfigurationProperties
@ConfigurationPropertiesScan(basePackages = "ru.dartanum.stock_analyzer")
public class StockAnalyzerApplication {
    public static void main(String[] args) {
        SpringApplication.run(StockAnalyzerApplication.class, args);
    }
}
