package com.kkulmoo.rebirth.card.domain;


import com.kkulmoo.rebirth.analysis.domain.enums.BenefitType;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

@Getter
@Builder
@ToString
public class BenefitTemplate {
    private Integer benefitId;

    private Integer cardTemplateId;

    private List<Integer> categoryId;

    private List<Integer> subcategoryId;

    private BenefitType benefitType;

    private Short merchantFilterType;

    private Short benefitConditionType;

    private List<Integer> performanceRange;

    private List<Double> benefitsBySection;

    private Boolean merchantInfo;

    private List<Integer> merchantList;

    private List<Integer> paymentRange;

    private List<Short> benefitUsageLimit;

    private List<Short> benefitUsageAmount;

    private DiscountType discountType;

    private String additionalInfo;
}