package com.kkulmoo.rebirth.analysis.infrastructure.repository;

import com.kkulmoo.rebirth.analysis.domain.dto.response.ReportCategoryDTO;
import com.kkulmoo.rebirth.analysis.infrastructure.entity.ReportCardCategoriesEntity;
import com.kkulmoo.rebirth.analysis.infrastructure.entity.ReportCardsEntity;
import com.kkulmoo.rebirth.recommend.domain.dto.response.AvgAmountByCategoryDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportCardCategoriesJpaRepository extends JpaRepository<ReportCardCategoriesEntity, Integer> {

    @Query("SELECT new com.kkulmoo.rebirth.analysis.domain.dto.response.ReportCategoryDTO(c.categoryName, " +
            "CAST(SUM(rcc.amount) AS int), " +
            "CAST(SUM(rcc.receivedBenefitAmount) AS int)) " +
            "FROM ReportCardCategoriesEntity rcc " +
            "JOIN ReportCardsEntity rc ON rcc.reportCard.reportCardId = rc.reportCardId " +
            "JOIN MonthlyTransactionSummaryEntity mts ON rc.reportId = mts.reportId " +
            "JOIN CategoryEntity c ON rcc.categoryId = c.categoryId " +
            "WHERE mts.userId = :userId " +
            "AND mts.year = :year " +
            "AND mts.month = :month " +
            "GROUP BY c.categoryName " +
            "ORDER BY SUM(rcc.amount) DESC")
    List<ReportCategoryDTO> getTotalSpendingByCategoryNameAndUser(
            @Param("userId") int userId,
            @Param("year") int year,
            @Param("month") int month
    );

    ReportCardCategoriesEntity getByReportCardAndCategoryId(ReportCardsEntity reportCard, int categoryId);

    List<ReportCardCategoriesEntity> getByReportCard(ReportCardsEntity reportCard);

    @Query(value = """
    SELECT rcc.category_id, 
            c.category_name,
            CAST(ABS(SUM(rcc.amount)) / 3 AS INT) AS avg_total_spending,
            CAST(ABS(SUM(rcc.amount)) AS INT) AS total_spending,
            CAST(SUM(rcc.received_benefit_amount) / 3 AS INT) AS avg_total_benefit,
            CAST(SUM(rcc.received_benefit_amount) AS INT) AS total_benefit
    FROM report_card_categories rcc
    JOIN report_cards rc ON rcc.report_card_id = rc.report_card_id
    JOIN monthly_transaction_summary mts ON rc.report_id = mts.report_id
    JOIN category c ON rcc.category_id = c.category_id
    WHERE (mts.year, mts.month) IN ( 
        (EXTRACT(YEAR FROM CURRENT_DATE - INTERVAL '1 month'), EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '1 month')),
        (EXTRACT(YEAR FROM CURRENT_DATE - INTERVAL '2 months'), EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '2 months')),
        (EXTRACT(YEAR FROM CURRENT_DATE - INTERVAL '3 months'), EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '3 months'))
    )
    AND mts.user_id = :userId
    GROUP BY rcc.category_id, c.category_name
    ORDER BY avg_total_spending DESC
    LIMIT 5
    """, nativeQuery = true)
    List<AvgAmountByCategoryDTO> getCategorySpendingLast3Months(Integer userId);
}
