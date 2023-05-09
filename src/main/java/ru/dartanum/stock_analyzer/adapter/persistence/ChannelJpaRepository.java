package ru.dartanum.stock_analyzer.adapter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.dartanum.stock_analyzer.domain.news.Channel;

public interface ChannelJpaRepository extends JpaRepository<Channel, Integer> {
    Channel findByName(String name);
}
