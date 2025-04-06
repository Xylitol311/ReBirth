package com.kkulmoo.rebirth.card.infrastructure.mapper;

import com.kkulmoo.rebirth.card.domain.BenefitTemplate;
import com.kkulmoo.rebirth.card.infrastructure.entity.BenefitTemplateEntity;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BenefitTemplateEntityMapper {

    // Entity -> Domain
    public BenefitTemplate toDomain(BenefitTemplateEntity entity) {
        if (entity == null) {
            return null;
        }

        return BenefitTemplate.builder()
                .benefitId(entity.getBenefitTemplateId())
                .cardTemplateId(entity.getCardTemplate() != null ? entity.getCardTemplate().getCardTemplateId() : null)
                .categoryId(entity.getCategoryIds())
                .subcategoryId(entity.getSubcategoryIds())
                .benefitType(entity.getBenefitType())
                .merchantFilterType(entity.getMerchantFilterType())
                .benefitConditionType(entity.getBenefitConditionType())
                .performanceRange(entity.getPerformanceRange())
                .benefitsBySection(entity.getBenefitsBySection())
                .merchantInfo(entity.getMerchantInfo())
                .merchantList(entity.getMerchantList())
                .paymentRange(entity.getPaymentRange())
                .benefitUsageLimit(entity.getBenefitUsageLimit())
                .benefitUsageAmount(entity.getBenefitUsageAmount())
                .discountType(entity.getDiscountType())
                .additionalInfo(entity.getAdditionalInfo())
                .build();
    }

    // Domain -> Entity
    public BenefitTemplateEntity toEntity(BenefitTemplate domain) {
        if (domain == null) {
            return null;
        }

        return BenefitTemplateEntity.builder()
                .benefitTemplateId(domain.getBenefitId())
                // cardTemplate은 별도로 설정해야 합니다
                .categoryIds(domain.getCategoryId())
                .subcategoryIds(domain.getSubcategoryId())
                .benefitType(domain.getBenefitType())
                .merchantFilterType(domain.getMerchantFilterType())
                .benefitConditionType(domain.getBenefitConditionType())
                .performanceRange(domain.getPerformanceRange())
                .benefitsBySection(domain.getBenefitsBySection())
                .merchantInfo(domain.getMerchantInfo())
                .merchantList(domain.getMerchantList())
                .paymentRange(domain.getPaymentRange())
                .benefitUsageLimit(domain.getBenefitUsageLimit())
                .benefitUsageAmount(domain.getBenefitUsageAmount())
                .discountType(domain.getDiscountType())
                .additionalInfo(domain.getAdditionalInfo())
                .build();
    }

    // Entity 리스트 -> Domain 리스트
    public List<BenefitTemplate> toDomainList(List<BenefitTemplateEntity> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }

        return entities.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    // Domain 리스트 -> Entity 리스트
    public List<BenefitTemplateEntity> toEntityList(List<BenefitTemplate> domains) {
        if (domains == null) {
            return Collections.emptyList();
        }

        return domains.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}