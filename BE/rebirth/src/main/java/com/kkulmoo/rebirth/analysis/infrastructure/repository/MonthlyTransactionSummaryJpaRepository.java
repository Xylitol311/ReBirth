package com.kkulmoo.rebirth.analysis.infrastructure.repository;

import com.kkulmoo.rebirth.analysis.infrastructure.entity.MonthlyTransactionSummaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MonthlyTransactionSummaryJpaRepository extends JpaRepository<MonthlyTransactionSummaryEntity, Integer> {

    MonthlyTransactionSummaryEntity getByUserId(int userId);

    @Query("SELECT mts " +
            "FROM MonthlyTransactionSummaryEntity mts " +
            "WHERE mts.userId = :userId " +
            "AND mts.year = :year " +
            "AND mts.month = :month")
    MonthlyTransactionSummaryEntity getByUserIdAndYearMonth(int userId, int year, int month);

    @Query("SELECT COALESCE(AVG(ms.receivedBenefitAmount), 0) " +
            "FROM MonthlyTransactionSummaryEntity ms " +
            "WHERE ABS(ms.totalSpending) BETWEEN :minSpending AND :maxSpending " +
            "AND ms.year = :year " +
            "AND ms.month = :month")
    Double getGroupBenefitAverage(@Param("year") int year,
                                  @Param("month") int month,
                                  @Param("minSpending") int minSpending,
                                  @Param("maxSpending") int maxSpending);

    List<MonthlyTransactionSummaryEntity> findByUserId(Integer userId);
}
