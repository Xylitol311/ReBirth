package com.kkulmoo.rebirth.card.infrastructure.repository;

import com.kkulmoo.rebirth.card.domain.BenefitRepository;
import com.kkulmoo.rebirth.card.infrastructure.entity.BenefitTemplateEntity;
import com.kkulmoo.rebirth.payment.application.BenefitInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class BenefitRepositoryImpl implements BenefitRepository {
    private final BenefitJpaRepository benefitJpaRepository;

    @Override
    public List<BenefitInfo> findByMerchantFilter(int cardTemplateId, int categoryId, int subcategoryId, int merchantId) {
        List<BenefitTemplateEntity> benefitTemplateEntities = benefitJpaRepository.findBenefitByTypeCondition(cardTemplateId, categoryId, subcategoryId, merchantId);
        List<BenefitInfo> benefitInfos = new ArrayList<>();
        for (BenefitTemplateEntity benefitTemplateEntity : benefitTemplateEntities) {
            BenefitInfo benefitInfo = BenefitInfo.builder()
                    .benefitId(benefitTemplateEntity.getBenefitTemplateId())
                    .cardTemplateId(cardTemplateId)
                    .benefitType(benefitTemplateEntity.getBenefitType())
                    .benefitConditionType(benefitTemplateEntity.getBenefitConditionType())
                    .merchantInfo(benefitTemplateEntity.getMerchantInfo())
                    .merchantFilterType(benefitTemplateEntity.getMerchantFilterType())
                    .merchantList(benefitTemplateEntity.getMerchantList())
                    .performanceRange(benefitTemplateEntity.getPerformanceRange())
                    .paymentRange(benefitTemplateEntity.getPaymentRange())
                    .benefitsBySection(benefitTemplateEntity.getBenefitsBySection())
                    .benefitUsageAmount(benefitTemplateEntity.getBenefitUsageAmount())
                    .benefitUsageLimit(benefitTemplateEntity.getBenefitUsageLimit())
                    .categoryId(benefitTemplateEntity.getCategoryIds())
                    .subcategoryId(benefitTemplateEntity.getSubcategoryIds())
                    .discountType(benefitTemplateEntity.getDiscountType())
                    .build();
            benefitInfos.add(benefitInfo);
        }
        return benefitInfos;
    }
}
