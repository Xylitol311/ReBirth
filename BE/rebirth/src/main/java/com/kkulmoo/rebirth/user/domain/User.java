package com.kkulmoo.rebirth.user.domain;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
public class User {
	private final UserId userId;
	private final Byte consumptionPatternId;
	private final String userName;
	private final String hashedPinNumber;
	private final String phoneNumber;
	private final String phoneSerialNumber;
	private final String userApiKey;
	private final LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private LocalDateTime deletedAt;
	private LocalDateTime latestLoadDataAt;

	@Builder
	public User(UserId userId, Byte consumptionPatternId, String userName, String hashedPinNumber,
		String phoneNumber, String phoneSerialNumber, String userApiKey,
		LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt,
		LocalDateTime latestLoadDataAt) {
		this.userId = userId;
		this.consumptionPatternId = consumptionPatternId;
		this.userName = userName;
		this.hashedPinNumber = hashedPinNumber;
		this.phoneNumber = phoneNumber;
		this.phoneSerialNumber = phoneSerialNumber;
		this.userApiKey = userApiKey;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.deletedAt = deletedAt;
		this.latestLoadDataAt = latestLoadDataAt;
	}


	public void delete() {
		this.deletedAt = LocalDateTime.now();
	}

	public boolean isDeleted() {
		return this.deletedAt != null;
	}

}