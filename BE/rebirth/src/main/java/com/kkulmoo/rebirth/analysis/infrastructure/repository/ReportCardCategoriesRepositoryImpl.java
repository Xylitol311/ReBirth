package com.kkulmoo.rebirth.analysis.infrastructure.repository;

import com.kkulmoo.rebirth.analysis.domain.repository.ReportCardCategoriesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReportCardCategoriesRepositoryImpl implements ReportCardCategoriesRepository {
    private final ReportCardCategoriesJpaRepository reportCardCategoriesJpaRepository;
}
