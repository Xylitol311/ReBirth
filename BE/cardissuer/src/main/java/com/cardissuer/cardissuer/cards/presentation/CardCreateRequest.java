package com.cardissuer.cardissuer.cards.presentation;

import java.sql.Timestamp;
import java.time.YearMonth;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CardCreateRequest {
	String userCI;
	String accountNumber;
	String cardNumber;
	String cardName;
	YearMonth expiryDate;
	String cvc;
	String cardPassword;
	Timestamp createdAt;
}
