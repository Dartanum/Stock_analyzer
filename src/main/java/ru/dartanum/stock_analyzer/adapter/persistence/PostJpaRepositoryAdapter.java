package ru.dartanum.stock_analyzer.adapter.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.dartanum.stock_analyzer.app.api.repository.PostRepository;
import ru.dartanum.stock_analyzer.domain.Channel;
import ru.dartanum.stock_analyzer.domain.Post;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class PostJpaRepositoryAdapter implements PostRepository {
    private final PostJpaRepository postJpaRepository;

    @Override
    public void saveAll(Set<Post> posts) {
        Set<Post> savedPosts = new HashSet<>(postJpaRepository.findAll());
        postJpaRepository.saveAll(diffSet(posts, savedPosts));
    }

    @Override
    public Map<Channel, LocalDateTime> findNewestPostByChannel() {
        List<Object[]> queryResult = postJpaRepository.findNewestPostByChannel();
        return queryResult.stream()
                .collect(Collectors.toMap(
                        entry -> new Channel((int) entry[0], (String) entry[1]),
                        entry -> ((Timestamp) entry[2]).toLocalDateTime()));
    }

    //set of elements contains elements from left set and not from right set
    private Set<Post> diffSet(Set<Post> left, Set<Post> right) {
        Set<Post> update = new HashSet<>(left);
        left.retainAll(right);
        update.removeAll(left);

        return update;
    }
}
