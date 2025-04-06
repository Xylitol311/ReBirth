package com.kkulmoo.rebirth.payment.domain;

import com.kkulmoo.rebirth.analysis.domain.enums.BenefitType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PreBenefit {
    private Integer userId;
    private Integer paymentCardId;
    private Integer recommendedCardId;
    private Integer amount;
    private BenefitType ifBenefitType;
    private Integer ifBenefitAmount;
    private BenefitType realBenefitType;
    private Integer realBenefitAmount;
    private String merchantName;

}
