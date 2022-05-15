package ru.dartanum.stock_analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.dartanum.stock_analyzer.domain.Channel;
import ru.dartanum.stock_analyzer.domain.Post;

import java.time.LocalDateTime;
import java.util.Set;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("from Post as p where p.creationDate >= :from and p.creationDate < :to")
    Set<Post> getInDateRange(LocalDateTime from, LocalDateTime to);

    Set<Post> findAllByChannel(Channel channel);
}
