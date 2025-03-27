package com.kkulmoo.rebirth.payment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
public class CardTemplate {


    private int cardTemplateId;

    private Short cardCompanyId;

    private String cardName;

    private String cardImgUrl;

    private String cardDeityName;

    private String cardDeityImgUrl;

    private int annualFee = 0;

    private String cardType;

    private String cardDetailInfo;

    private String cardConstellationInfo;

    private String benefitConditions;

    private String lastMonthUsageRanges;

    private String godImgUrl;

    private String godName;

    @Builder
    public CardTemplate(int cardTemplateId, Short cardCompanyId, String cardName, String cardImgUrl, String cardDeityName, String cardDeityImgUrl, int annualFee, String cardType, String cardDetailInfo, String cardConstellationInfo, String benefitConditions, String lastMonthUsageRanges, String godImgUrl, String godName) {
        this.cardTemplateId = cardTemplateId;
        this.cardCompanyId = cardCompanyId;
        this.cardName = cardName;
        this.cardImgUrl = cardImgUrl;
        this.cardDeityName = cardDeityName;
        this.cardDeityImgUrl = cardDeityImgUrl;
        this.annualFee = annualFee;
        this.cardType = cardType;
        this.cardDetailInfo = cardDetailInfo;
        this.cardConstellationInfo = cardConstellationInfo;
        this.benefitConditions = benefitConditions;
        this.lastMonthUsageRanges = lastMonthUsageRanges;
        this.godImgUrl = godImgUrl;
        this.godName = godName;
    }
}
