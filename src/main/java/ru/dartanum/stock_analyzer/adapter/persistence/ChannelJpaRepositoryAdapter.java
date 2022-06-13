package ru.dartanum.stock_analyzer.adapter.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.dartanum.stock_analyzer.app.api.repository.ChannelRepository;
import ru.dartanum.stock_analyzer.domain.Channel;
import ru.dartanum.stock_analyzer.framework.spring.ValuesProperties;

import javax.annotation.PostConstruct;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Repository
@RequiredArgsConstructor
public class ChannelJpaRepositoryAdapter implements ChannelRepository {
    private final ChannelJpaRepository channelJpaRepository;
    private final ValuesProperties valuesProperties;

    @Override
    public List<Channel> findAll() {
        return channelJpaRepository.findAll();
    }

    @Override
    public Channel findByName(String name) {
        return channelJpaRepository.findByName(name);
    }

    @PostConstruct
    private void loadChannels() {
        List<Channel> savedChannels = channelJpaRepository.findAll();
        List<String> savedChannelNames = savedChannels.stream().map(Channel::getName).collect(toList());
        List<String> channelNames = valuesProperties.getChannels();

        List<Channel> channelsForDeleting = savedChannels.stream()
                .filter(channel -> !channelNames.contains(channel.getName()))
                .collect(toList());

        List<Channel> channelsForSaving = channelNames.stream()
                .filter(channelName -> !savedChannelNames.contains(channelName))
                .map(channelName -> Channel.builder().name(channelName).build())
                .collect(toList());

        channelJpaRepository.deleteAll(channelsForDeleting);
        channelJpaRepository.saveAll(channelsForSaving);
    }
}
