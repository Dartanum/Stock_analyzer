package ru.dartanum.stock_analyzer.adapter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.dartanum.stock_analyzer.domain.technical.Model;

public interface ModelJpaRepository extends JpaRepository<Model, String> {
}
