package com.kkulmoo.rebirth.user.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
@AllArgsConstructor
public class UserCardBenefit {
    private Integer userCardBenefitId;
    private Integer userId;
    private Integer benefitTemplateId;
    private Short spendingTier;
    private Short benefitCount;
    private Integer benefitAmount;
    private LocalDateTime updateDate;
    private Integer year;
    private Integer month;
}
