package com.kkulmoo.rebirth.card.domain;

import com.kkulmoo.rebirth.user.domain.UserId;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class MyCard {
	private Integer cardId;
	private UserId userId;
	private Integer cardTemplateId;
	private String cardUniqueNumber;
	private String accountNumber;
	private Short cardOrder;
	private String cardName;
	private Integer annualFee;
	private String permanentToken;
	private Short paymentCardOrder;
	private Short spendingTier;
	private Short payCount;
	private LocalDate expiryDate;
	private Short isExpired;
	private LocalDateTime paymentCreatedAt;
	private LocalDateTime createdAt;
	private LocalDateTime deletedAt;
	private LocalDateTime latestLoadDataAt;

	public void changeCardOrder(Short newOrder) {
		this.cardOrder = newOrder;
	}

	// 결제 카드 순서 변경 메소드
	public void changePaymentCardOrder(Short newOrder) {
		this.paymentCardOrder = newOrder;
	}

	public MyCard updateLatestLoadDataAt() {
		this.latestLoadDataAt = LocalDateTime.now();
		return this;
	}
}