package com.kkulmoo.rebirth.card.infrastructure.repository;

import com.kkulmoo.rebirth.card.domain.BenefitRepository;
import com.kkulmoo.rebirth.payment.application.BenefitInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class BenefitRepositoryImpl implements BenefitRepository {
    private final BenefitJpaRepository benefitJpaRepository;

    @Override
    public List<BenefitInfo> findBenefitsByMerchantFilter(int cardTemplateId, int categoryId, int subcategoryId, int merchantId) {
        return benefitJpaRepository.findBenefitsByTypeCondition(cardTemplateId, categoryId, subcategoryId, merchantId).stream()
                .map(entity -> BenefitInfo.builder()
                        .benefitId(entity.getBenefitTemplateId())
                        .cardTemplateId(cardTemplateId)
                        .benefitType(entity.getBenefitType())
                        .benefitConditionType(entity.getBenefitConditionType())
                        .merchantInfo(entity.getMerchantInfo())
                        .merchantFilterType(entity.getMerchantFilterType())
                        .merchantList(entity.getMerchantList())
                        .performanceRange(entity.getPerformanceRange())
                        .paymentRange(entity.getPaymentRange())
                        .benefitsBySection(entity.getBenefitsBySection())
                        .benefitUsageAmount(entity.getBenefitUsageAmount())
                        .benefitUsageLimit(entity.getBenefitUsageLimit())
                        .categoryId(entity.getCategoryIds())
                        .subcategoryId(entity.getSubcategoryIds())
                        .discountType(entity.getDiscountType())
                        .build())
                .collect(Collectors.toList());
    }
}
