package com.kkulmoo.rebirth.analysis.infrastructure.repository;

import com.kkulmoo.rebirth.analysis.domain.dto.response.ReportCategoryDTO;
import com.kkulmoo.rebirth.analysis.infrastructure.entity.ReportCardCategoriesEntity;
import com.kkulmoo.rebirth.analysis.infrastructure.entity.ReportCardsEntity;
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
}
