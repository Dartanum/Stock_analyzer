package ru.dartanum.stock_analyzer.adapter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.dartanum.stock_analyzer.domain.news.Channel;
import ru.dartanum.stock_analyzer.domain.news.Post;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface PostJpaRepository extends JpaRepository<Post, Long> {

    @Query("from Post as p where p.creationDate >= :from and p.creationDate < :to")
    Set<Post> getInDateRange(LocalDateTime from, LocalDateTime to);

    @Query(value = "select c.id, c.name, max(creation_date) from post inner join channel c on c.id = channel_id group by c.id, c.name", nativeQuery = true)
    List<Object[]> findNewestPostByChannel();

    Set<Post> findAllByChannel(Channel channel);
}
