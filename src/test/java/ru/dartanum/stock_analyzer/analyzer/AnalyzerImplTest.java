package ru.dartanum.stock_analyzer.analyzer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.dartanum.stock_analyzer.app.api.repository.PostRepository;
import ru.dartanum.stock_analyzer.framework.spring.StockAnalyzerApplication;
import ru.dartanum.stock_analyzer.app.impl.analyzer.AnalyzerImpl;
import ru.dartanum.stock_analyzer.domain.Category;
import ru.dartanum.stock_analyzer.domain.Channel;
import ru.dartanum.stock_analyzer.domain.Post;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = StockAnalyzerApplication.class)
class AnalyzerImplTest {
    private static final String POSITIVE_TEXT = "#BTC #NDX #корреляция #наблюдние  #крипто #акции #техи положительная корреляция BTC - Nasdaq 100 = рекорд";
    private static final String POSITIVE_TEXT_2 = "#BTC #крипто #прогноз VanEck: курс BTC сможет вырасти до $250 000 через несколько лет";
    private static final String NEGATIVE_TEXT = "падение биткоина ужас";
    private static final String NEUTRAL_TEXT = "без б куда";

    @Autowired
    private AnalyzerImpl analyzerImpl;
    @Autowired
    private PostRepository postRepository;

    @Test
    void analyze_positive() {
        Post post = new Post();
        post.setContent(POSITIVE_TEXT_2);
        Post updatedPost = analyzerImpl.analyze(Set.of(post)).iterator().next();
        System.out.println(updatedPost.getProbability());
        assertEquals(Category.POSITIVE, updatedPost.getCategory());
    }

    @Test
    void analyze_negative() {
        Post post = new Post();
        post.setContent(NEGATIVE_TEXT);
        Post updatedPost = analyzerImpl.analyze(Set.of(post)).iterator().next();
        System.out.println(updatedPost.getProbability());
        assertEquals(Category.NEGATIVE, updatedPost.getCategory());
    }

    @Test
    void analyze_neutral() {
        Post post = new Post();
        post.setContent(NEUTRAL_TEXT);
        Post updatedPost = analyzerImpl.analyze(Set.of(post)).iterator().next();
        System.out.println(updatedPost.getProbability());
        assertEquals(Category.NEUTRAL, updatedPost.getCategory());
    }

    @Test
    void testRepository() {
        Map<Channel, LocalDateTime> newestPostByChannel = postRepository.findNewestPostByChannel();
    }
}
