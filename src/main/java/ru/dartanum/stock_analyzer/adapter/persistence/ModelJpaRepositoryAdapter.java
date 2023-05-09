package ru.dartanum.stock_analyzer.adapter.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.dartanum.stock_analyzer.app.api.repository.ModelRepository;
import ru.dartanum.stock_analyzer.domain.technical.Model;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ModelJpaRepositoryAdapter implements ModelRepository {
    private final ModelJpaRepository modelJpaRepository;

    @Override
    public Optional<Model> findByFigi(String figi) {
        return modelJpaRepository.findById(figi);
    }

    @Override
    public Model save(Model model) {
        return modelJpaRepository.save(model);
    }
}
