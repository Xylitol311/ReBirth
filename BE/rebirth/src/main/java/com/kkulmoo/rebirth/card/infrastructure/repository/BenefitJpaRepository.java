package com.kkulmoo.rebirth.card.infrastructure.repository;

import com.kkulmoo.rebirth.card.infrastructure.entity.BenefitTemplateEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BenefitJpaRepository extends JpaRepository<BenefitTemplateEntity, Integer> {
    @Query("SELECT b " +
            "FROM BenefitTemplateEntity b " +
            "WHERE b.cardTemplate.cardTemplateId = :cardTemplateId " +
            "AND ( " +
            "    b.merchantFilterType = 1 " +
            "    OR (b.merchantFilterType = 2 AND (:subcategoryId MEMBER OF b.subcategoryIds OR :categoryId MEMBER OF b.categoryIds)) " +
            "    OR (b.merchantFilterType = 3 AND :merchantId MEMBER OF b.merchantList) " +
            ")")
    List<BenefitTemplateEntity> findBenefitByTypeCondition(
            @Param("cardTemplateId") int cardTemplateId,
            @Param("categoryId") int categoryId,
            @Param("subcategoryId") int subcategoryId,
            @Param("merchantId") int merchantId);

}
