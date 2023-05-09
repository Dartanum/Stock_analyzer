package ru.dartanum.stock_analyzer.util;

import com.github.demidko.aot.PartOfSpeech;
import com.github.demidko.aot.WordformMeaning;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import ru.dartanum.stock_analyzer.app.impl.analyzer.news.DataPreparator;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static com.github.demidko.aot.PartOfSpeech.*;
import static com.github.demidko.aot.PartOfSpeech.Infinitive;
import static com.github.demidko.aot.WordformMeaning.lookupForMeanings;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.util.FileCopyUtils.copyToString;

@SpringBootTest(classes = {DataPreparator.class})
class NormalizeLearningData {
    private static final String POSITIVE_WORDS_OUTPUT_PATH = "./positive_prepared.txt";

    @Autowired
    private DataPreparator dataPreparator;

    @Value("classpath:learning/positive.txt")
    private Resource positiveWordsSource;
    @Value("classpath:learning/negative.txt")
    private Resource negativeWordsSource;

    @Test
    void prepareData() throws IOException {
        Reader wordsReader = new InputStreamReader(positiveWordsSource.getInputStream(), UTF_8);
        Writer wordsWriter = new FileWriter(POSITIVE_WORDS_OUTPUT_PATH);

        List<String> dataList = Arrays.stream(copyToString(wordsReader).trim().split("[\n\r]+")).collect(Collectors.toList());


        prepare(dataList).forEach(word -> {
            try {
                wordsWriter.write(word + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private Set<String> prepare(List<String> dataList) {
        Set<PartOfSpeech> validPartOfSpeech = Set.of(Adjective, Noun, Verb, Infinitive);
        List<List<WordformMeaning>> wordFormMeanings = dataList.stream().map(str -> {
            try {
                return lookupForMeanings(str);
            } catch (IOException e) {
                return new ArrayList<WordformMeaning>();
            }
        }).toList();

        Set<String> result = new TreeSet<>();
        wordFormMeanings.forEach(listOfMeanings -> {
            if (!listOfMeanings.isEmpty()) {
                listOfMeanings.stream()
                        .filter(entry -> validPartOfSpeech.contains(entry.getPartOfSpeech()))
                        .forEach(entry -> entry.getLemma().getTransformations().forEach(transformation -> result.add(transformation.toString())));
            }
        });

        return result;
    }
}
