package com.kkulmoo.rebirth.analysis.infrastructure.repository;

import com.kkulmoo.rebirth.analysis.infrastructure.entity.ConsumptionPatternEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsumptionPatternJpaRepository extends JpaRepository<ConsumptionPatternEntity, String> {

}
