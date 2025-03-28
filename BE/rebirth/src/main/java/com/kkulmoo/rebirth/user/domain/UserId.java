package com.kkulmoo.rebirth.user.domain;

import lombok.Getter;
import lombok.Value;

@Value
public class UserId {
	Integer value;

	public UserId(Integer value) {
		validateUserId(value);
		this.value = value;
	}

	private void validateUserId(Integer value) {
		if (value == null || value <= 0) {
			throw new IllegalArgumentException("사용자 ID가 존재하지 않습니다.");
		}
	}
}