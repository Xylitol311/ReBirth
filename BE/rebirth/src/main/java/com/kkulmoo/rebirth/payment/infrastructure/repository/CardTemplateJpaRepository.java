package com.kkulmoo.rebirth.payment.infrastructure.repository;

import com.kkulmoo.rebirth.shared.entity.CardTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import java.util.Optional;

// 카드 템플릿
public interface CardTemplateJpaRepository extends JpaRepository<CardTemplateEntity, Integer> {
    Optional<CardTemplateEntity> findByCardName(String cardName);

    @Query(value = """
    SELECT DISTINCT c.* 
    FROM card_templates c
    JOIN benefit_templates b ON c.card_template_id = b.card_template_id
    JOIN card_companies cc ON cc.card_company_id = c.card_company_id
    LEFT JOIN category cat ON cat.category_id = ANY(b.category_id)
    WHERE (:benefitTypes IS NULL OR COALESCE(b.benefit_type, '') IN (:benefitTypes))
      AND (:cardCompanyNames IS NULL OR COALESCE(cc.company_name, '') IN (:cardCompanyNames))  
      AND (:categoryNames IS NULL OR COALESCE(cat.category_name, '') IN (:categoryNames))  
      AND (:minPerformanceRange IS NULL OR :maxPerformanceRange IS NULL 
           OR COALESCE(b.performance_range[1], 0) BETWEEN :minPerformanceRange AND :maxPerformanceRange)
      AND (:minAnnualFee IS NULL OR :maxAnnualFee IS NULL 
           OR COALESCE(c.annual_fee, 0) BETWEEN :minAnnualFee AND :maxAnnualFee)
    """, nativeQuery = true)
    List<CardTemplateEntity> searchCards(
            @Param("benefitTypes") List<String> benefitTypes,
            @Param("cardCompanyNames") List<String> cardCompanyNames,
            @Param("categoryNames") List<String> categoryNames,
            @Param("minPerformanceRange") Integer minPerformanceRange,
            @Param("maxPerformanceRange") Integer maxPerformanceRange,
            @Param("minAnnualFee") Integer minAnnualFee,
            @Param("maxAnnualFee") Integer maxAnnualFee
    );

}
