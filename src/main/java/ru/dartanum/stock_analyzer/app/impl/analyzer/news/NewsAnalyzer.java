package ru.dartanum.stock_analyzer.app.impl.analyzer.news;

import de.daslaboratorium.machinelearning.classifier.Classification;
import de.daslaboratorium.machinelearning.classifier.bayes.BayesClassifier;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import ru.dartanum.stock_analyzer.domain.news.Category;
import ru.dartanum.stock_analyzer.domain.news.Post;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;

import static java.lang.Math.abs;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.springframework.util.FileCopyUtils.copyToString;
import static ru.dartanum.stock_analyzer.domain.news.Category.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewsAnalyzer {
    private static final float INACCURACY_PERCENT = 1f;

    private final BayesClassifier<String, Category> bayesClassifier;
    private final DataPreparator dataPreparator;

    @Value("classpath:learning/positive.txt")
    private Resource positiveWordsSource;
    @Value("classpath:learning/negative.txt")
    private Resource negativeWordsSource;

    public Set<Post> analyze(Set<Post> data) {
        data.forEach(post -> {
            Classification<String, Category> result = bayesClassifier.classifyDetailed(prepareContent(post))
                    .stream()
                    .reduce((classification, classification2) -> {
                        float positiveProb = classification.getProbability() * 1000;
                        float negativeProb = classification2.getProbability() * 1000;
                        float percentageDifferent = abs(positiveProb / negativeProb * 100 - 100);
                        log.info(post.getId() + " - " + percentageDifferent + "%");
                        if (percentageDifferent <= INACCURACY_PERCENT) {
                            return new Classification<>(classification.getFeatureset(), NEUTRAL);
                        } else {
                            return positiveProb > negativeProb ? classification : classification2;
                        }
                    })
                    .orElseThrow();
            post.setCategory(result.getCategory());
        });

        return data;
    }

    public Pair<Category, Float> getVerdict(Set<Post> data) {
        List<Pair<Category, Long>> stats = new LinkedList<>();
        Arrays.stream(Category.values()).forEach(category -> stats.add(Pair.of(
                category,
                data.stream().filter(post -> post.getCategory() == category).count())));
        var maxBySize = Collections.max(stats, Comparator.comparingLong(Pair::getSecond));

        if (stats.stream().filter(entry -> entry.getSecond().equals(maxBySize.getSecond())).map(Pair::getSecond).count() > 1) {
            return Pair.of(UNDEFINED, 1.0f);
        }

        return Pair.of(maxBySize.getFirst(), (float) maxBySize.getSecond() / data.size());
    }

    @PostConstruct
    protected void prepare() {
        try {
            bayesClassifier.learn(POSITIVE, asList(getWordsFromFile(positiveWordsSource)));
            bayesClassifier.learn(Category.NEGATIVE, asList(getWordsFromFile(negativeWordsSource)));

            log.info("Classifier has learned");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String[] getWordsFromFile(Resource file) throws IOException {
        Reader wordsReader = new InputStreamReader(file.getInputStream(), UTF_8);
        return copyToString(wordsReader).trim().split("[\n\r]+");
    }

    private List<String> prepareContent(Post post) {
        List<String> tokenizedWords = dataPreparator.tokenize(post.getContent());
        List<String> normalizedWords = dataPreparator.normalize(tokenizedWords);

        return dataPreparator.lemmatization(normalizedWords);
    }
}
