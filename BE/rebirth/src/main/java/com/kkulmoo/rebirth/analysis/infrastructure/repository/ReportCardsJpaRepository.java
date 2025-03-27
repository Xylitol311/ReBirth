package com.kkulmoo.rebirth.analysis.infrastructure.repository;

import com.kkulmoo.rebirth.analysis.infrastructure.entity.ReportCardsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportCardsJpaRepository extends JpaRepository<ReportCardsEntity, Integer> {

    ReportCardsEntity getByReportIdAndCardId(int reportId, int cardId);
}
