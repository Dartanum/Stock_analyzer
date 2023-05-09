package ru.dartanum.stock_analyzer.framework.spring;

import de.daslaboratorium.machinelearning.classifier.bayes.BayesClassifier;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import ru.dartanum.stock_analyzer.domain.news.Category;
import ru.tinkoff.piapi.core.InvestApi;

import java.time.Duration;

@Configuration
@EnableJpaRepositories(basePackages = "ru.dartanum.stock_analyzer.adapter.persistence")
@ComponentScan(basePackages = "ru.dartanum.stock_analyzer")
@EntityScan(basePackages = "ru.dartanum.stock_analyzer.domain")
public class AppConfiguration {

    @Bean
    public WebDriver webDriver(@Value("${chromedriver}") String driverLocation) {
        System.setProperty("webdriver.chrome.driver", driverLocation);
        var options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts()
                .pageLoadTimeout(Duration.ofSeconds(10))
                .implicitlyWait(Duration.ofSeconds(5))
                .scriptTimeout(Duration.ofSeconds(5));

        return driver;
    }

    @Bean
    public BayesClassifier<String, Category> bayesClassifier(@Value("${values.classifier.capacity}") int capacity) {
        BayesClassifier<String, Category> classifier = new BayesClassifier<>();
        classifier.setMemoryCapacity(capacity);

        return classifier;
    }

    @Bean
    public InvestApi investApi(@Value("${tinkoff.api.sandbox.token}") String token) {
        System.setProperty("java.awt.headless", "false");
        var api = InvestApi.createSandbox(token);
        api.getSandboxService().openAccountSync();

        return api;
    }
}
