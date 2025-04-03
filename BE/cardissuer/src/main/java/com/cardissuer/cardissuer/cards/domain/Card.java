package com.cardissuer.cardissuer.cards.domain;

import java.sql.Timestamp;
import java.time.YearMonth;
import java.util.Date;

import com.cardissuer.cardissuer.transaction.presentation.PermanentTokenRequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card {
	private CardUniqueNumber cardUniqueNumber;
	private String userCI;
	private String accountNumber;
	private String cardNumber;
	private String cardName;
	private YearMonth expiryDate;
	private String cvc;
	private String cardPassword;
	private Timestamp createdAt;
	private Timestamp deletedAt;

}