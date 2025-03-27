package com.kkulmoo.rebirth.analysis.infrastructure.repository;

import com.kkulmoo.rebirth.analysis.infrastructure.entity.ReportCardCategoriesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportCardCategoriesJpaRepository extends JpaRepository<ReportCardCategoriesEntity, Integer> {

    @Query("SELECT c.categoryName, SUM(rcc.amount) " +
            "FROM ReportCardCategoriesEntity rcc " +
            "JOIN ReportCardsEntity rc ON rcc.reportCardId = rc.reportCardId " +
            "JOIN MonthlyTransactionSummaryEntity mts ON rc.reportId = mts.reportId " +
            "JOIN CategoryEntity c ON rcc.categoryId = c.categoryId " +
            "WHERE mts.userId = :userId " +
            "AND mts.year = :year " +
            "AND mts.month = :month " +
            "GROUP BY c.categoryName " +
            "ORDER BY SUM(rcc.amount) DESC")
    List<Object[]> getTotalSpendingByCategoryNameAndUser(
            @Param("userId") int userId,
            @Param("year") int year,
            @Param("month") int month
    );

    ReportCardCategoriesEntity getByReportCardIdAndCategoryId(int reportCardId, int categoryId);
}
