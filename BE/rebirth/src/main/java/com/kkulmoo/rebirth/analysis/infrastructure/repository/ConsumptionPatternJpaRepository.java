package com.kkulmoo.rebirth.analysis.infrastructure.repository;

import com.kkulmoo.rebirth.analysis.infrastructure.entity.ConsumptionPatternEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsumptionPatternJpaRepository extends JpaRepository<ConsumptionPatternEntity, String> {

}
