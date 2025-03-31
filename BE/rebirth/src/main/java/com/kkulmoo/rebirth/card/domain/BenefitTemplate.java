package com.kkulmoo.rebirth.card.domain;


import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class BenefitTemplate {
    private Integer benefitId;

    private Integer cardTemplateId;

    private List<Integer> categoryId;

    private List<Integer> subcategoryId;

    private BenefitType benefitType;

    private Short benefitMethod;

    private Integer[] performanceRange;

    private Double[] performanceRangeByBenefit;

    private Boolean merchantInfo;

    private String[] merchantList;

    private Integer[] paymentRange;

    private Short[] benefitUsageLimit;

    private Short[] benefitUsageAmount;

    private DiscountType discountType;

    private String additionalInfo;
}