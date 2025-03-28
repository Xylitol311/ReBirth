package com.kkulmoo.rebirth.card.domain;

import lombok.Value;

@Value
public class CardId {
	Integer value;

	public CardId(Integer value) {
		validateCardId(value);
		this.value = value;
	}

	private void validateCardId(Integer value) {
		if (value == null || value <= 0) {
			throw new IllegalArgumentException("유효하지 않은 카드 ID입니다.");
		}
	}
}