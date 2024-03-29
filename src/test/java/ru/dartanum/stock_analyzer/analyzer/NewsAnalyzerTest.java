package ru.dartanum.stock_analyzer.analyzer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.Pair;
import ru.dartanum.stock_analyzer.app.api.repository.PostRepository;
import ru.dartanum.stock_analyzer.app.impl.analyzer.news.NewsAnalyzer;
import ru.dartanum.stock_analyzer.domain.news.Category;
import ru.dartanum.stock_analyzer.domain.news.Channel;
import ru.dartanum.stock_analyzer.domain.news.Post;
import ru.dartanum.stock_analyzer.framework.spring.StockAnalyzerApplication;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.dartanum.stock_analyzer.domain.news.Category.*;

@SpringBootTest(classes = StockAnalyzerApplication.class)
class NewsAnalyzerTest {
    private static final String POSITIVE_TEXT = "#BTC #NDX #корреляция #наблюдние  #крипто #акции #техи положительная корреляция BTC - Nasdaq 100 = рекорд";
    private static final String POSITIVE_TEXT_2 = "#BTC #крипто #прогноз VanEck: курс BTC сможет вырасти до $250 000 через несколько лет";
    private static final String NEGATIVE_TEXT = "падение биткоина ужас";
    private static final String NEUTRAL_TEXT = "без б куда";

    private static final int CHANNEL_ID_1 = 1;
    private static final int CHANNEL_ID_2 = 2;
    private static final int CHANNEL_ID_3 = 3;

    private static final Channel CHANNEL_1 = Channel.builder().id(CHANNEL_ID_1).build();
    private static final Channel CHANNEL_2 = Channel.builder().id(CHANNEL_ID_2).build();
    private static final Channel CHANNEL_3 = Channel.builder().id(CHANNEL_ID_3).build();

    private static final Post POST_1 = Post.builder().category(POSITIVE).channel(CHANNEL_1).creationDate(LocalDateTime.now().minusDays(1)).build();
    private static final Post POST_2 = Post.builder().category(POSITIVE).channel(CHANNEL_2).creationDate(LocalDateTime.now()).build();
    private static final Post POST_3 = Post.builder().category(NEGATIVE).channel(CHANNEL_2).creationDate(LocalDateTime.now().minusDays(3)).build();
    private static final Post POST_4 = Post.builder().category(NEUTRAL).channel(CHANNEL_3).creationDate(LocalDateTime.now().minusDays(2)).build();
    private static final Post POST_5 = Post.builder().category(NEGATIVE).channel(CHANNEL_2).creationDate(LocalDateTime.now()).build();

    private static final Set<Post> posts = new HashSet<>();

    @BeforeAll
    private static void setUp() {
        posts.add(Post.builder().content("Правительство Германии готовится выделить бывшему подразделению «Газпрома» Gazprom Germania помощь в размере до 10 млрд евро, чтобы спасти его, сообщает Bloomberg.").channel(CHANNEL_1).creationDate(LocalDateTime.now()).build());
        posts.add(Post.builder().content(" \"Газпром\" подает газ для Европы через Украину в объеме 41,9 млн куб. м через \"Суджу\", заявка через \"Сохрановку\" отклонена Украиной, — ТАСС#GAZP #газ@IF_Market_News45257611111").channel(CHANNEL_1).creationDate(LocalDateTime.now().minusDays(1)).build());
        posts.add(Post.builder().content("#газ #транзит #украина #европа #россия \"Газпром\" подает газ для Европы через Украину в объеме 41,9 млн куб. м через \"Суджу\", заявка через \"Сохрановку\" отклонена").channel(CHANNEL_2).creationDate(LocalDateTime.now().minusDays(2)).build());
        posts.add(Post.builder().content(" \"Газпром\" подает газ для Европы через Украину в объеме 41,9 млн куб. м через \"Суджу\", заявка через \"Сохрановку\" отклонена Украиной, — ТАСС#GAZP #газ@IF_Market_News443251").channel(CHANNEL_2).creationDate(LocalDateTime.now().minusDays(3)).build());
        posts.add(Post.builder().content("Газпром сокращает поставки газа в Италию на 15% • Газпром усиливает давление на европейских потребителей газа: вчера компания объявила о сокращение поставок газа по Северному потока на 40%. #РФ @IF_Market_News11216105331").channel(CHANNEL_3).creationDate(LocalDateTime.now().minusDays(4)).build());
    }

    @Autowired
    private NewsAnalyzer newsAnalyzer;
    @Autowired
    private PostRepository postRepository;

    @Test
    void analyze_positive() {
        Post post = new Post();
        post.setContent(POSITIVE_TEXT_2);
        Post updatedPost = newsAnalyzer.analyze(Set.of(post)).iterator().next();
        assertEquals(POSITIVE, updatedPost.getCategory());
    }

    @Test
    void analyze_negative() {
        Post post = new Post();
        post.setContent(NEGATIVE_TEXT);
        Post updatedPost = newsAnalyzer.analyze(Set.of(post)).iterator().next();
        assertEquals(Category.NEGATIVE, updatedPost.getCategory());
    }

    @Test
    void analyze_neutral() {
        Post post = new Post();
        post.setContent(NEUTRAL_TEXT);
        Post updatedPost = newsAnalyzer.analyze(Set.of(post)).iterator().next();
        assertEquals(Category.NEUTRAL, updatedPost.getCategory());
    }

    @Test
    void getVerdict_UNDEFINED() {
        Set<Post> data = Set.of(POST_1, POST_2, POST_3, POST_4, POST_5);

        Pair<Category, Float> verdict = newsAnalyzer.getVerdict(data);

        assertEquals(UNDEFINED, verdict.getFirst());
    }

    @Test
    void getVerdict_POSITIVE() {
        Set<Post> data = Set.of(POST_1, POST_2, POST_3);

        Pair<Category, Float> verdict = newsAnalyzer.getVerdict(data);

        assertEquals(POSITIVE, verdict.getFirst());
        assertEquals(67, Math.round(verdict.getSecond() * 100f));
    }

    @Test
    void execute() {
        Set<Post> result = newsAnalyzer.analyze(posts);
    }
}
