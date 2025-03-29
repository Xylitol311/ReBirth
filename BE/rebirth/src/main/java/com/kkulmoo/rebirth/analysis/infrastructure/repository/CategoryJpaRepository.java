package com.kkulmoo.rebirth.analysis.infrastructure.repository;

import com.kkulmoo.rebirth.analysis.infrastructure.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryJpaRepository extends JpaRepository<CategoryEntity, Integer> {
}
