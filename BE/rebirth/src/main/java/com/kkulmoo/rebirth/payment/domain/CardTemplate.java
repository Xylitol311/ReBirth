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
@Builder
public class CardTemplate {


    private int cardTemplateId;

    private Short cardCompanyId;

    private String cardName;

    private String cardImgUrl;

    private String cardDeityName;

    private String cardDeityImgUrl;

    private int annualFee;

    private String cardType;

    private String cardDetailInfo;

    private String cardConstellationInfo;

    private String benefitConditions;

    private String lastMonthUsageRanges;

    private String godImgUrl;

    private String godName;

}
