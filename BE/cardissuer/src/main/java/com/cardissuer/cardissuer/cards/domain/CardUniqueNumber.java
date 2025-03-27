package com.cardissuer.cardissuer.cards.domain;

import java.util.UUID;
import java.util.regex.Pattern;

import lombok.Value;

@Value
public class CardUniqueNumber {
	private final String value;

	public CardUniqueNumber(String value) {
		this.value = value;
	}

	public static CardUniqueNumber of(String value) {
		if (value == null || value.trim().isEmpty()) {
			return new CardUniqueNumber(generateUUID());
		}
		validate(value);
		return new CardUniqueNumber(value);
	}

	private static String generateUUID() {
		return UUID.randomUUID().toString();
	}

	private static void validate(String value) {
		// UUID 형식 검증 (표준 UUID 형식: 8-4-4-4-12 형태의 16진수)
		String uuidPattern = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

		if (!Pattern.matches(uuidPattern, value)) {
			throw new IllegalArgumentException("유효하지 않은 UUID 형식입니다.");
		}
	}
}