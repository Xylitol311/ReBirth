package com.kkulmoo.rebirth.payment.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CardTemplate {

    private int cardTemplateId;

    private Short cardCompanyId;

    private String cardName;

    private String cardImgUrl;

    private String cardDetailInfo;

    private String cardConstellationInfo;

    private Integer annualFee;

    private String cardType;
}
