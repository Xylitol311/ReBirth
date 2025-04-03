package com.kkulmoo.rebirth.payment.application;

import com.kkulmoo.rebirth.analysis.domain.enums.BenefitType;
import com.kkulmoo.rebirth.card.domain.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
public class BenefitInfo {
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
}
