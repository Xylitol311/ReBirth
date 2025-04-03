package com.kkulmoo.rebirth.analysis.infrastructure.repository;

import com.kkulmoo.rebirth.card.domain.BenefitTemplate;
import com.kkulmoo.rebirth.card.infrastructure.entity.BenefitTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BenefitTemplateJpaRepository extends JpaRepository<BenefitTemplateEntity, Integer> {

    @Query(value = """
    SELECT * 
    FROM benefit_templates 
    WHERE :categoryId = ANY(category_id)
    OR category_id IS NULL
    """, nativeQuery = true)
    List<BenefitTemplateEntity> findByCategoryId(Integer categoryId);
}
