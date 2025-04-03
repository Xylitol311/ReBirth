package com.kkulmoo.rebirth.card.domain;


import com.kkulmoo.rebirth.analysis.domain.enums.BenefitType;
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

    private Short merchantFilterType;

    private Short benefitConditionType;

    private Integer[] performanceRange;

    private Double[] benefitsBySection;

    private Boolean merchantInfo;

    private String[] merchantList;

    private Integer[] paymentRange;

    private Short[] benefitUsageLimit;

    private Short[] benefitUsageAmount;

    private DiscountType discountType;

    private String additionalInfo;


}