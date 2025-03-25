package com.kkulmoo.rebirth.card.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class CardTemplate {
	private Integer cardTemplateId;
	private Integer cardCompanyId;
	private String cardName;
	private String cardImgUrl;
	private String godName;
	private String godImgUrl;
	private Integer annualFee;
	private String cardType;
	private Short spendingMaxTier;
	private Integer maxPerformanceAmount;
	private String benefitText;
}