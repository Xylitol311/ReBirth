package com.kkulmoo.rebirth.payment.infrastructure.mapper;

import com.kkulmoo.rebirth.payment.domain.UserCardBenefit;
import com.kkulmoo.rebirth.payment.infrastructure.entity.UserCardBenefitEntity;
import org.springframework.stereotype.Component;

@Component
public class UserCardBenefitEntityMapper {
    public UserCardBenefit toUserCardBenefit(UserCardBenefitEntity userCardBenefitEntity) {
        return UserCardBenefit.builder()
                .userId(userCardBenefitEntity.getUserId())
                .benefitTemplateId(userCardBenefitEntity.getBenefitTemplateId())
                .spendingTier(userCardBenefitEntity.getSpendingTier())
                .benefitCount(userCardBenefitEntity.getBenefitCount())
                .benefitAmount(userCardBenefitEntity.getBenefitAmount())
                .resetDate(userCardBenefitEntity.getResetDate())
                .updateDate(userCardBenefitEntity.getUpdateDate())
                .build();
    }

    public UserCardBenefitEntity toUserCardBenefitEntity(UserCardBenefit userCardBenefit) {
        return UserCardBenefitEntity.builder()
                .userId(userCardBenefit.getUserId())
                .benefitTemplateId(userCardBenefit.getBenefitTemplateId())
                .spendingTier(userCardBenefit.getSpendingTier())
                .benefitCount(userCardBenefit.getBenefitCount())
                .benefitAmount(userCardBenefit.getBenefitAmount())
                .resetDate(userCardBenefit.getResetDate())
                .updateDate(userCardBenefit.getUpdateDate())
                .build();
    }
}
