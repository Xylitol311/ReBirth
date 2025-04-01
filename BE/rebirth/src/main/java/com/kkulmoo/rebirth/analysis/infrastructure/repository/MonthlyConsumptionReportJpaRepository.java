package com.kkulmoo.rebirth.analysis.infrastructure.repository;

import com.kkulmoo.rebirth.analysis.infrastructure.entity.MonthlyConsumptionReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MonthlyConsumptionReportJpaRepository extends JpaRepository<MonthlyConsumptionReportEntity, Integer> {

}
