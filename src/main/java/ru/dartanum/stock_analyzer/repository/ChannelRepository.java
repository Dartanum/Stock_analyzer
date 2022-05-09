package ru.dartanum.stock_analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.dartanum.stock_analyzer.domain.Channel;

public interface ChannelRepository extends JpaRepository<Channel, Integer> {

    Channel findByName(String name);
}
