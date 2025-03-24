package com.kkulmoo.rebirth.user.domain;

import lombok.Value;

@Value
public class UserId {
	Integer value;
	public UserId(Integer value) {
		this.value = value;
	}

	private void validateUserId(String value) {
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalArgumentException("사용자 ID가 존재하지 않습니다.");
		}
	}

}
