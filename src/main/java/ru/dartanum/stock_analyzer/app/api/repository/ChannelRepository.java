package ru.dartanum.stock_analyzer.app.api.repository;

import ru.dartanum.stock_analyzer.domain.Channel;

import java.util.List;

public interface ChannelRepository {
    List<Channel> findAll();

    Channel findByName(String name);
}
