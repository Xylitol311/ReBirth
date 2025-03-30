package com.kkulmoo.rebirth.card.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.kkulmoo.rebirth.user.domain.UserId;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Card {
	private Integer cardId;
	private UserId userId;
	private Integer cardTemplateId;
	private Integer annualFee;
	private String cardUniqueNumber;
	private LocalDate expiryDate;
	private Short cardOrder;
	private Short isExpired;
	private String permanentToken;
	private Short paymentCardOrder;
	private LocalDateTime paymentCreatedAt;
	private LocalDateTime createdAt;
	private LocalDateTime deletedAt;

}