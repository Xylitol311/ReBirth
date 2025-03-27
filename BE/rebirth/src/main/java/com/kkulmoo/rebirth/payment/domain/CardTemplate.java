package com.kkulmoo.rebirth.payment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CardTemplate {


    private int cardTemplateId;
    private Short cardCompanyId;
    private String cardName;
    private String cardImgUrl;
    private String godName;
    private String godImgUrl;
    private int annualFee;
    private String cardType;
    private Short spendingMaxTier;
    private int maxPerformanceAmount;

    @Builder
    public CardTemplate(int cardTemplateId, Short cardCompanyId, String cardName, String cardImgUrl, String godName, String godImgUrl, int annualFee, String cardType, Short spendingMaxTier, int maxPerformanceAmount) {
        this.cardTemplateId = cardTemplateId;
        this.cardCompanyId = cardCompanyId;
        this.cardName = cardName;
        this.cardImgUrl = cardImgUrl;
        this.godName = godName;
        this.godImgUrl = godImgUrl;
        this.annualFee = annualFee;
        this.cardType = cardType;
        this.spendingMaxTier = spendingMaxTier;
        this.maxPerformanceAmount = maxPerformanceAmount;
    }
}
