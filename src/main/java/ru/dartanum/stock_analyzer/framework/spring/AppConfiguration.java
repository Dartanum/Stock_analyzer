package ru.dartanum.stock_analyzer.framework.spring;

import de.daslaboratorium.machinelearning.classifier.bayes.BayesClassifier;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import ru.dartanum.stock_analyzer.domain.Category;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableJpaRepositories(basePackages = "ru.dartanum.stock_analyzer.adapter.persistence")
@ComponentScan(basePackages = "ru.dartanum.stock_analyzer")
@EntityScan(basePackages = "ru.dartanum.stock_analyzer.domain")
public class AppConfiguration {

    @Bean
    public WebDriver webDriver(@Value("${chromedriver}") String driverLocation) {
        System.setProperty("webdriver.chrome.driver", driverLocation);
        WebDriver driver = new ChromeDriver();
        driver.manage().timeouts()
                .pageLoadTimeout(10, TimeUnit.SECONDS)
                .implicitlyWait(5, TimeUnit.SECONDS)
                .setScriptTimeout(5, TimeUnit.SECONDS);

        return driver;
    }

    @Bean
    public BayesClassifier<String, Category> bayesClassifier(@Value("${values.classifier.capacity}") int capacity) {
        BayesClassifier<String, Category> classifier = new BayesClassifier<>();
        classifier.setMemoryCapacity(capacity);

        return classifier;
    }
}
