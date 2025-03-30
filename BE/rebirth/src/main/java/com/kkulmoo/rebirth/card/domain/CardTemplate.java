package com.kkulmoo.rebirth.card.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
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

}
