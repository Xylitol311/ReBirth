package com.kkulmoo.rebirth.user.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@ToString
public class User {
	private final UserId userId;
	private final String consumptionPatternId;
	private final String userName;
	private final String hashedPinNumber;
	private final String phoneNumber;
	private final String phoneSerialNumber;
	private final String userCI;
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

	public void updateBankAccounts(List<String> newBankAccounts) {
		// 현재 bankAccounts 참조를 새 리스트로 교체
		this.bankAccounts.clear();
		if (newBankAccounts != null) {
			this.bankAccounts.addAll(newBankAccounts);
		}
	}
	public void updateLatestLoadDataAtNow(){
		this.bankLatestLoadDataAt = LocalDateTime.now();
	}
}