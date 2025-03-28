package com.cardissuer.cardissuer.cards.domain;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import com.cardissuer.cardissuer.cards.infrastructure.CardEntity;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PermanentToken {

	String cardUniqueNumber;
	String token;
	LocalDateTime createdAt;
	Boolean isActive;
	Card card;
}
