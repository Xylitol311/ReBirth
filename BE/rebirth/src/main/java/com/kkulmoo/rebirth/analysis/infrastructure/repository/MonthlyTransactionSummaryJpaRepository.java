package com.kkulmoo.rebirth.analysis.infrastructure.repository;

import com.kkulmoo.rebirth.analysis.infrastructure.entity.MonthlyTransactionSummaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MonthlyTransactionSummaryJpaRepository extends JpaRepository<MonthlyTransactionSummaryEntity, Integer> {

    MonthlyTransactionSummaryEntity getByUserId(int userId);


    MonthlyTransactionSummaryEntity getByUserIdAndYearMonth(int userId, int year, int month);

    @Query("SELECT AVG(ms.receivedBenefitAmount)" +
            "FROM MonthlyTransactionSummaryEntity ms " +
            "WHERE ms.totalSpending BETWEEN :minSpending AND :maxSpending")
    Double getGroupBenefitAverage(int minSpending, int maxSpending);
}
