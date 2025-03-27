package com.kkulmoo.rebirth.analysis.infrastructure.repository;

import com.kkulmoo.rebirth.analysis.domain.repository.MonthlyTransactionSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MonthlyTransactionSummaryRepositoryImpl implements MonthlyTransactionSummaryRepository {
    private final MonthlyTransactionSummaryJpaRepository monthlyTransactionSummaryJpaRepository;
}
