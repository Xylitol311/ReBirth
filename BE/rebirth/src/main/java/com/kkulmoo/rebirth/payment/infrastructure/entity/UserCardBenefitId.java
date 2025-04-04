package com.kkulmoo.rebirth.payment.infrastructure.entity;

import lombok.Data;

import java.io.Serializable;
@Data
public class UserCardBenefitId implements Serializable {
    private Integer userId;
    private Integer benefitTemplateId;
}
