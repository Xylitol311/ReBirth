package com.kkulmoo.rebirth.card.domain;

import com.kkulmoo.rebirth.user.domain.UserId;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class myCard {
	private Integer cardId;
	private UserId userId;
	private Integer cardTemplateId;
	private String cardUniqueNumber;
	private String accountNumber;
	private Short cardOrder;
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
}