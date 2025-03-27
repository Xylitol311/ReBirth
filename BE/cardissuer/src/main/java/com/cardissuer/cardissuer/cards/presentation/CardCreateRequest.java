package com.cardissuer.cardissuer.cards.presentation;

import java.sql.Timestamp;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CardCreateRequest {
	String userCI;
	String accountNumber;
	Timestamp createdAt;
}
