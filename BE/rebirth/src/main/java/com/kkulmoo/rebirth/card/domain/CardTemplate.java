package com.kkulmoo.rebirth.card.domain;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CardTemplate {


	private int cardTemplateId;

	private Short cardCompanyId;

	private String cardName;

	private String cardImgUrl;

	private int annualFee;

	private String cardType;

	private String cardDetailInfo;

	private String cardConstellationInfo;

	private List<Integer> performanceRange;
}
