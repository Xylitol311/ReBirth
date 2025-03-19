package com.cardissuer.cardissuer.cards;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardCreateRequestDto {
	private Integer userId;
	private String cardNumber;
	private String expiryDate; // "MM/YY" 형식
	private String cvc;
	private Integer annualFee;
}
