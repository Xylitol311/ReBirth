package com.kkulmoo.rebirth.payment.domain;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Builder
@Getter
@AllArgsConstructor
public class UserCardBenefit {
    private Integer userId;
    private Integer benefitTemplateId;
    private Short spendingTier;
    private Short benefitCount;
    private Integer benefitAmount;
    private LocalDateTime resetDate;
    private LocalDateTime updateDate;
}
