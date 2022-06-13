package ru.dartanum.stock_analyzer.app.impl.analyzer;

import de.daslaboratorium.machinelearning.classifier.Classification;
import de.daslaboratorium.machinelearning.classifier.bayes.BayesClassifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import ru.dartanum.stock_analyzer.app.api.analyzer.Analyzer;
import ru.dartanum.stock_analyzer.domain.Category;
import ru.dartanum.stock_analyzer.domain.Post;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.springframework.util.FileCopyUtils.copyToString;
import static ru.dartanum.stock_analyzer.domain.Category.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyzerImpl implements Analyzer {
    private static final float INACCURACY_PERCENT = 5f;

    private final BayesClassifier<String, Category> bayesClassifier;

    @Value("classpath:learning/positive.txt")
    private Resource positiveWordsSource;
    @Value("classpath:learning/negative.txt")
    private Resource negativeWordsSource;

    @Override
    public Set<Post> analyze(Set<Post> data) {
        data.forEach(post -> {
            Classification<String, Category> result = bayesClassifier.classifyDetailed(Arrays.stream(post.getContent().split("\\s"))
                            .filter(str -> !str.startsWith("#"))
                            .map(str -> str.replaceAll("[^а-яА-Я]", ""))
                            .filter(str -> str.matches("[а-яА-Я]+"))
                            .collect(Collectors.toList()))
                    .stream()
                    .reduce((classification, classification2) -> {
                        float prob1 = classification.getProbability();
                        float prob2 = classification2.getProbability();
                        float percentageDifferent = abs(prob1 / prob2 * 100 - 100);

                        if (percentageDifferent <= INACCURACY_PERCENT) {
                            return new Classification<>(classification.getFeatureset(), NEUTRAL);
                        } else {
                            return prob1 > prob2 ? classification : classification2;
                        }
                    })
                    .orElseThrow();
            post.setCategory(result.getCategory());
        });

        return data;
    }

    @Override
    public Pair<Category, Float> getVerdict(Set<Post> data) {
        List<Pair<Category, Long>> stats = new LinkedList<>();
        Arrays.stream(Category.values()).forEach(category -> stats.add(Pair.of(
                category,
                data.stream().filter(post -> post.getCategory() == category).count()))
        );
        var maxBySize = Collections.max(stats, Comparator.comparingLong(Pair::getSecond));

        if (stats.stream().filter(entry -> entry.getSecond().equals(maxBySize.getSecond())).map(Pair::getSecond).count() > 1) {
            return Pair.of(UNDEFINED, 1.0f);
        }

        return Pair.of(maxBySize.getFirst(), (float) maxBySize.getSecond() / data.size());
    }

    @PostConstruct
    protected void prepare() {
        try {
            Reader positiveWordsReader = new InputStreamReader(positiveWordsSource.getInputStream(), UTF_8);
            String[] positiveWords = copyToString(positiveWordsReader).trim().split("[\n\r]+");
            bayesClassifier.learn(POSITIVE, asList(positiveWords));

            Reader negativeWordsReader = new InputStreamReader(negativeWordsSource.getInputStream(), UTF_8);
            String[] negativeWords = copyToString(negativeWordsReader).split("[\r\n]+");
            bayesClassifier.learn(Category.NEGATIVE, asList(negativeWords));

            log.info("Classifier has learned");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
