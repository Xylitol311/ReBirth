package com.kkulmoo.rebirth.analysis.domain.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PreBenefitDto {
    private Integer userId;
    private Integer paymentCardId;
    private Integer recommendedCardId;
    private Integer amount;
    private String ifBenefitType;
    private Integer ifBenefitAmount;
    private String realBenefitType;
    private Integer realBenefitAmount;
    private String paymentCardImgUrl;
    private String recommendedCardImgUrl;
    private String merchantName;
    private boolean isGood;
}
