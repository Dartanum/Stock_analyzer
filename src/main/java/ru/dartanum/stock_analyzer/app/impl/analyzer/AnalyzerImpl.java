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
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;
import static org.springframework.util.FileCopyUtils.copyToString;
import static ru.dartanum.stock_analyzer.domain.Category.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyzerImpl implements Analyzer {
    private final BayesClassifier<String, Category> bayesClassifier;

    @Value("classpath:learning/positive.txt")
    private Resource positiveWordsSource;
    @Value("classpath:learning/negative.txt")
    private Resource negativeWordsSource;
    @Value("classpath:learning/neutral.txt")
    private Resource neutralWordsSource;

    @Override
    public Set<Post> analyze(Set<Post> data) {
        data.forEach(post -> {
            Classification<String, Category> result = bayesClassifier.classify(Arrays.stream(post.getContent().split("\\s"))
                    .filter(str -> !str.startsWith("#"))
                    .map(str -> str.replaceAll("[^а-яА-Я]", ""))
                    .filter(str -> str.matches("[а-яА-Я]+"))
                    .collect(Collectors.toList()));
            post.setCategory(result.getCategory());
            post.setProbability(result.getProbability());
        });

        return data;
    }

    @Override
    public Pair<Category, Float> getVerdict(Set<Post> data) {
        Pair<Category, Float> result;
        List<Pair<Category, Pair<Integer, Float>>> stats = new LinkedList<>();
        Arrays.stream(values()).forEach(category -> stats.add(Pair.of(category, getStats(data, category))));
        var maxBySize = Collections.max(stats, Comparator.comparingInt(o -> o.getSecond().getFirst()));
        var maxByWeight = Collections.max(stats, (o1, o2) -> Float.compare(o1.getSecond().getSecond(), o2.getSecond().getSecond()));
        float totalWeight = data.stream().map(Post::getProbability).reduce((res, weight) -> res += weight).orElse(0f);

        if (maxByWeight.getSecond().getSecond() > maxBySize.getSecond().getSecond() * 2) {
            result = Pair.of(maxByWeight.getFirst(), maxByWeight.getSecond().getSecond() / totalWeight);
        } else {
            result = Pair.of(maxBySize.getFirst(), (float) maxBySize.getSecond().getFirst() / data.size());
        }

        return result;
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

            Reader neutralWordsReader = new InputStreamReader(neutralWordsSource.getInputStream(), UTF_8);
            String[] neutralWords = copyToString(neutralWordsReader).split("[\r\n]+");
            bayesClassifier.learn(Category.NEUTRAL, asList(neutralWords));
            log.info("Classifier has learned");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Pair<Integer, Float> getStats(Set<Post> data, Category category) {
        Set<Post> filteredPosts = data.stream().filter(post -> post.getCategory() == category).collect(toSet());
        float weight = filteredPosts.stream().map(Post::getProbability).reduce((res, item) -> res += item).orElse(0f);

        return Pair.of(filteredPosts.size(), weight);
    }
}
