package ru.dartanum.stock_analyzer.configuration;

import de.daslaboratorium.machinelearning.classifier.bayes.BayesClassifier;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.dartanum.stock_analyzer.domain.Category;

import java.util.concurrent.TimeUnit;

@Configuration
public class AppConfiguration {

    @Bean
    public WebDriver webDriver(@Value("${chromedriver}") String driverLocation) {
        System.setProperty("webdriver.chrome.driver", driverLocation);
        WebDriver driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        return driver;
    }

    @Bean
    public BayesClassifier<String, Category> bayesClassifier(@Value("${values.classifier.capacity}") int capacity) {
        BayesClassifier<String, Category> classifier = new BayesClassifier<>();
        classifier.setMemoryCapacity(capacity);

        return classifier;
    }
}
