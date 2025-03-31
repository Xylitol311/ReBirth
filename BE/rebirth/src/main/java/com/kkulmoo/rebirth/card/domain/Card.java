package com.kkulmoo.rebirth.card.domain;

import com.kkulmoo.rebirth.user.domain.UserId;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class Card {
	private Integer cardId;
	private UserId userId;
	private Integer cardTemplateId;
	private String cardNumber;
	private String cardUniqueNumber;
	private LocalDate expiryDate;
	private Short cardOrder;
	private Short isExpired;
	private String permanentToken;
	private Short paymentCardOrder;
	private Integer annualFee;
	private LocalDateTime paymentCreatedAt;
	private LocalDateTime createdAt;
	private LocalDateTime deletedAt;

}