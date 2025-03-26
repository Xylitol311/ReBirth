package com.cardissuer.cardissuer.cards.domain;

import java.sql.Timestamp;
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
	private Integer userId;
	private String cardNumber;
	private String cardName;
	private Date expiryDate;
	private String cvc;
	private String cardPassword;
	private Timestamp createdAt;
	private Timestamp deletedAt;

	// Card 업데이트 메서드
	public Card updateCard(PermanentTokenRequest request) {
		return Card.builder()
			.cardUniqueNumber(this.cardUniqueNumber) // 기존 값 유지
			.userId(this.userId) // 기존 값 유지
			.cardNumber(request.getCardNumber() != null ? request.getCardNumber() : this.cardNumber) // 새 값이 있으면 업데이트, 없으면 기존 값 유지
			.cardName(this.cardName) // 기존 값 유지
			.expiryDate(this.expiryDate) // 기존 값 유지
			.cvc(request.getCvc() != null ? request.getCvc() : this.cvc) // 새 값이 있으면 업데이트, 없으면 기존 값 유지
			.cardPassword(request.getPassword() != null ? request.getPassword() : this.cardPassword) // 새 값이 있으면 업데이트, 없으면 기존 값 유지
			.createdAt(this.createdAt) // 기존 값 유지
			.deletedAt(this.deletedAt) // 기존 값 유지
			.build();
	}
}