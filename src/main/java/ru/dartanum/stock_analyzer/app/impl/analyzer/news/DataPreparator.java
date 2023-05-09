package ru.dartanum.stock_analyzer.app.impl.analyzer.news;

import com.github.demidko.aot.PartOfSpeech;
import com.github.demidko.aot.WordformMeaning;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.github.demidko.aot.PartOfSpeech.*;
import static com.github.demidko.aot.WordformMeaning.lookupForMeanings;

@Slf4j
@Component
public class DataPreparator {

    public List<String> tokenize(String data) {
        return Arrays.stream(data.split("\\s"))
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }

    public List<String> normalize(List<String> data) {
        return data.stream()
                .filter(str -> !(str.startsWith("#") || str.startsWith("@")))
                .map(str -> str.replaceAll("[^а-яА-Я]", ""))
                .filter(str -> str.matches("[а-яА-Я]+") && str.length() > 2)
                .collect(Collectors.toList());
    }

    public List<String> lemmatization(List<String> data) {
        Set<PartOfSpeech> validPartOfSpeech = Set.of(Adjective, Noun, Verb, Infinitive);
        List<List<WordformMeaning>> wordFormMeanings = data.stream().map(str -> {
            try {
                return lookupForMeanings(str);
            } catch (IOException e) {
                log.warn("Exception while getting lemma for word {}", str);
                return new ArrayList<WordformMeaning>();
            }
        }).toList();

        List<String> result = new ArrayList<>();
        wordFormMeanings.forEach(listOfMeanings -> {
            if (!listOfMeanings.isEmpty()) {
                listOfMeanings.stream()
                        .filter(entry -> validPartOfSpeech.contains(entry.getPartOfSpeech()))
                        .findFirst()
                        .ifPresent(entry -> result.add(entry.getLemma().toString()));
            }
        });

        return result;
    }
}
