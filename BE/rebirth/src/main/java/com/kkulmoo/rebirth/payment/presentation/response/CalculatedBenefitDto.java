package com.kkulmoo.rebirth.payment.presentation.response;

import com.kkulmoo.rebirth.analysis.domain.enums.BenefitType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CalculatedBenefitDto {
    private Integer myCardId;
    private String permanentToken;
    private Integer benefitId;
    private int benefitAmount;
    private BenefitType benefitType;
}
