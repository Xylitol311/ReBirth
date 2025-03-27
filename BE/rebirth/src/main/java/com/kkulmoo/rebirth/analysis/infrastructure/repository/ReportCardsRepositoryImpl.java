package com.kkulmoo.rebirth.analysis.infrastructure.repository;

import com.kkulmoo.rebirth.analysis.domain.repository.ReportCardsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReportCardsRepositoryImpl implements ReportCardsRepository {
    private final ReportCardsJpaRepository reportCardsJpaRepository;
}
