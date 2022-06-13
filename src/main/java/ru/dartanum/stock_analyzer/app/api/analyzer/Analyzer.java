package ru.dartanum.stock_analyzer.app.api.analyzer;

import org.springframework.data.util.Pair;
import ru.dartanum.stock_analyzer.domain.Category;
import ru.dartanum.stock_analyzer.domain.Post;

import java.util.Set;

public interface Analyzer {
    Set<Post> analyze(Set<Post> data);

    Pair<Category, Float> getVerdict(Set<Post> data);
}
