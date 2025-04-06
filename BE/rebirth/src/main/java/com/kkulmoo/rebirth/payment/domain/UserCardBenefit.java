package com.kkulmoo.rebirth.payment.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
@AllArgsConstructor
public class UserCardBenefit {
    private Integer userId;
    private Integer benefitTemplateId;
    private Short spendingTier;
    private Short benefitCount;
    //todo: 수홍이형이 고쳐야한느 부분입니다;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    private Short benefitAmount;
    private LocalDateTime resetDate;
    private LocalDateTime updateDate;
}
