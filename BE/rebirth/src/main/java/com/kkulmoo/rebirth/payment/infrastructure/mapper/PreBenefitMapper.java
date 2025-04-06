package com.kkulmoo.rebirth.payment.infrastructure.mapper;

import com.kkulmoo.rebirth.payment.domain.PreBenefit;
import com.kkulmoo.rebirth.payment.infrastructure.entity.PreBenefitEntity;
import org.springframework.stereotype.Component;

@Component
public class PreBenefitMapper {

    public PreBenefitEntity toEntity(PreBenefit domain) {
        if (domain == null) return null;
        return PreBenefitEntity.builder()
                .userId(domain.getUserId())
                .paymentCardId(domain.getPaymentCardId())
                .recommendedCardId(domain.getRecommendedCardId())
                .amount(domain.getAmount())
                .ifBenefitType(domain.getIfBenefitType())
                .ifBenefitAmount(domain.getIfBenefitAmount())
                .realBenefitType(domain.getRealBenefitType())
                .realBenefitAmount(domain.getRealBenefitAmount())
                .merchantName(domain.getMerchantName())
                .build();
    }

    public PreBenefit toDomain(PreBenefitEntity entity) {
        if (entity == null) return null;
        return PreBenefit.builder()
                .userId(entity.getUserId())
                .paymentCardId(entity.getPaymentCardId())
                .recommendedCardId(entity.getRecommendedCardId())
                .amount(entity.getAmount())
                .ifBenefitType(entity.getIfBenefitType())
                .ifBenefitAmount(entity.getIfBenefitAmount())
                .realBenefitType(entity.getRealBenefitType())
                .realBenefitAmount(entity.getRealBenefitAmount())
                .merchantName(entity.getMerchantName())
                .build();
    }
}
