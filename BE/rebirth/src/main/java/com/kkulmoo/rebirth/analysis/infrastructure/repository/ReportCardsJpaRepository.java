package com.kkulmoo.rebirth.analysis.infrastructure.repository;

import com.kkulmoo.rebirth.analysis.infrastructure.entity.ReportCardsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReportCardsJpaRepository extends JpaRepository<ReportCardsEntity, Integer> {

    ReportCardsEntity getByReportIdAndCardId(int reportId, int cardId);

    List<ReportCardsEntity> getByReportId(int reportId);

    Optional<ReportCardsEntity> findTopByCardIdOrderByCreatedAtDesc(Integer cardId);
}
