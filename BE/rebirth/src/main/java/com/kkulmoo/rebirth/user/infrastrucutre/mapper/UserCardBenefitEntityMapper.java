package com.kkulmoo.rebirth.user.infrastrucutre.mapper;

import com.kkulmoo.rebirth.user.domain.UserCardBenefit;
import com.kkulmoo.rebirth.user.infrastrucutre.entity.UserCardBenefitEntity;
import org.springframework.stereotype.Component;

@Component
public class UserCardBenefitEntityMapper {
    public UserCardBenefit toUserCardBenefit(UserCardBenefitEntity userCardBenefitEntity) {
        return UserCardBenefit.builder()
                .userCardBenefitId(userCardBenefitEntity.getUserCardBenefitId())
                .userId(userCardBenefitEntity.getUserId())
                .benefitTemplateId(userCardBenefitEntity.getBenefitTemplateId())
                .spendingTier(userCardBenefitEntity.getSpendingTier())
                .benefitCount(userCardBenefitEntity.getBenefitCount())
                .benefitAmount(userCardBenefitEntity.getBenefitAmount())
                .updateDate(userCardBenefitEntity.getUpdateDate())
                .year(userCardBenefitEntity.getYear())
                .month(userCardBenefitEntity.getMonth())
                .build();
    }

    public UserCardBenefitEntity toUserCardBenefitEntity(UserCardBenefit userCardBenefit) {
        return UserCardBenefitEntity.builder()
                .userCardBenefitId(userCardBenefit.getUserCardBenefitId())
                .userId(userCardBenefit.getUserId())
                .benefitTemplateId(userCardBenefit.getBenefitTemplateId())
                .spendingTier(userCardBenefit.getSpendingTier())
                .benefitCount(userCardBenefit.getBenefitCount())
                .benefitAmount(userCardBenefit.getBenefitAmount())
                .updateDate(userCardBenefit.getUpdateDate())
                .year(userCardBenefit.getYear())
                .month(userCardBenefit.getMonth())
                .build();
    }
}
