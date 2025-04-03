package com.kkulmoo.rebirth.payment.presentation.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class CalculatedBenefitDto {
    private final Integer myCardId;
    private final String permanentToken;
    private final Integer benefitId;
    private final int benefitAmount;
    private final String benefitType;
}
