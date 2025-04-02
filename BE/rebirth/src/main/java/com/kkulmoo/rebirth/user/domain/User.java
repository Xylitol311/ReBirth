package com.kkulmoo.rebirth.user.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class User {
	private final UserId userId;
	private final String consumptionPatternId;
	private final String userName;
	private final String hashedPinNumber;
	private final String phoneNumber;
	private final String phoneSerialNumber;
	private final String userApiKey;
	private final LocalDateTime createdAt;
	private final List<String> bankAccounts;
	private LocalDateTime updatedAt;
	private LocalDateTime deletedAt;
	private LocalDateTime bankLatestLoadDataAt;

	public void delete() {
		this.deletedAt = LocalDateTime.now();
	}
	public boolean isDeleted() {
		return this.deletedAt != null;
	}
}