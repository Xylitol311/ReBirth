package com.kkulmoo.rebirth.card.infrastructure.repository;

import com.kkulmoo.rebirth.card.infrastructure.entity.BenefitTemplateEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BenefitJpaRepository extends JpaRepository<BenefitTemplateEntity, Integer> {
    @Query(value = "SELECT * FROM benefit_templates b " +
            "WHERE b.card_template_id = :cardTemplateId " +
            "AND ( " +
            "    b.merchant_filter_type = 1 " +
            "    OR (b.merchant_filter_type = 2 AND (:subcategoryId = ANY(b.subcategory_id) OR :categoryId = ANY(b.category_id))) " +
            "    OR (b.merchant_filter_type = 3 AND :merchantId = ANY(b.merchant_list)) " +
            ")",
            nativeQuery = true)
    List<BenefitTemplateEntity> findBenefitsByTypeCondition(
            @Param("cardTemplateId") int cardTemplateId,
            @Param("categoryId") int categoryId,
            @Param("subcategoryId") int subcategoryId,
            @Param("merchantId") int merchantId);

}
