package ru.dartanum.stock_analyzer.analyzer;

import de.daslaboratorium.machinelearning.classifier.Classification;
import de.daslaboratorium.machinelearning.classifier.bayes.BayesClassifier;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import ru.dartanum.stock_analyzer.domain.Category;
import ru.dartanum.stock_analyzer.domain.Post;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.springframework.util.FileCopyUtils.copyToString;

@Component
@RequiredArgsConstructor
public class Analyzer {
    private final BayesClassifier<String, Category> bayesClassifier;

    @Value("classpath:learning/positive_words_ru.txt")
    private Resource positiveWordsSource;
    @Value("classpath:learning/negative_words_ru.txt")
    private Resource negativeWordsSource;
    @Value("classpath:learning/neutral_words_ru.txt")
    private Resource neutralWordsSource;

    public Set<Post> analyze(Set<Post> data) {
        data.forEach(post -> {
            Classification<String, Category> result = bayesClassifier.classify(Arrays.stream(post.getContent().split("\\s"))
                    .map(str -> str.replaceAll("[^а-яА-Я]", ""))
                    .filter(str -> str.matches("[а-яА-Я]+"))
                    .collect(Collectors.toList()));
            post.setCategory(result.getCategory());
            post.setProbability(result.getProbability());
        });

        return data;
    }

    @PostConstruct
    protected void prepare() {
        try {
            Reader positiveWordsReader = new InputStreamReader(positiveWordsSource.getInputStream(), UTF_8);
            String[] positiveWords = copyToString(positiveWordsReader).trim().split("[\n\r]+");
            bayesClassifier.learn(Category.POSITIVE, asList(positiveWords));

            Reader negativeWordsReader = new InputStreamReader(negativeWordsSource.getInputStream(), UTF_8);
            String[] negativeWords = copyToString(negativeWordsReader).split("[\r\n]+");
            bayesClassifier.learn(Category.NEGATIVE, asList(negativeWords));

            Reader neutralWordsReader = new InputStreamReader(neutralWordsSource.getInputStream(), UTF_8);
            String[] neutralWords = copyToString(neutralWordsReader).split("[\r\n]+");
            bayesClassifier.learn(Category.NEUTRAL, asList(neutralWords));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
