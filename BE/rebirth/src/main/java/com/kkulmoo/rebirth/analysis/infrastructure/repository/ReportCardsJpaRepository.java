package com.kkulmoo.rebirth.analysis.infrastructure.repository;

import com.kkulmoo.rebirth.analysis.domain.dto.response.MainCardSummaryDTO;
import com.kkulmoo.rebirth.analysis.infrastructure.entity.ReportCardsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReportCardsJpaRepository extends JpaRepository<ReportCardsEntity, Integer> {

    ReportCardsEntity getByReportIdAndCardId(int reportId, int cardId);

    List<ReportCardsEntity> getByReportId(int reportId);

    Optional<ReportCardsEntity> findTopByCardIdOrderByCreatedAtDesc(Integer cardId);

    @Query("SELECT new com.kkulmoo.rebirth.analysis.domain.dto.response.MainCardSummaryDTO(" +
            "ct.cardName, ct.cardImgUrl, ABS(rc.monthSpendingAmount), " +
            "rc.monthBenefitAmount, ct.annualFee) " +
            "FROM  ReportCardsEntity rc " +
            "JOIN MonthlyTransactionSummaryEntity mts ON rc.reportId = mts.reportId " +
            "JOIN CardEntity c ON c.cardId = rc.cardId " +
            "JOIN CardTemplateEntity ct ON ct.cardTemplateId = c.cardTemplateId " +
            "WHERE mts.userId = :userId " +
            "AND mts.year = :year " +
            "AND mts.month = :month")
    List<MainCardSummaryDTO> getByUserIdAndYearAndMonth(Integer userId, int year, int month);

    @Query("SELECT rc " +
            "FROM ReportCardsEntity rc " +
            "JOIN MonthlyTransactionSummaryEntity mts ON mts.reportId = rc.reportId " +
            "WHERE mts.userId = :userId " +
            "AND mts.year = :year " +
            "AND mts.month = :month " +
            "AND rc.cardId = :cardId")
    Optional<ReportCardsEntity> getByUserIdAndCardIdAndYearAndMonth(Integer userId, Integer cardId, Integer year, Integer month);

}
