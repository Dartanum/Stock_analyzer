package ru.dartanum.stock_analyzer.app.api.repository;

import ru.dartanum.stock_analyzer.domain.technical.Model;

import java.util.Optional;

public interface ModelRepository {
    Optional<Model> findByFigi(String figi);

    Model save(Model model);
}
