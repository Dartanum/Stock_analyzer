package ru.dartanum.stock_analyzer.app.api.parser;

import ru.dartanum.stock_analyzer.domain.Post;

import java.util.Set;

public interface Parser {
    Set<Post> parse();
}
