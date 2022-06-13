package ru.dartanum.stock_analyzer.app.api.repository;

import ru.dartanum.stock_analyzer.domain.Channel;
import ru.dartanum.stock_analyzer.domain.Post;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

public interface PostRepository {
    void saveAll(Set<Post> posts);

    Map<Channel, LocalDateTime> findNewestPostByChannel();
}
