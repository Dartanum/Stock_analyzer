package ru.dartanum.stock_analyzer.app.api.repository;

import ru.dartanum.stock_analyzer.domain.news.Channel;
import ru.dartanum.stock_analyzer.domain.news.Post;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

public interface PostRepository {
    void saveAll(Set<Post> posts);

    Map<Channel, LocalDateTime> findNewestPostByChannel();
}
