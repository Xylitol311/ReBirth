package com.cardissuer.cardissuer.transaction.domain;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import com.cardissuer.cardissuer.cards.domain.CardUniqueNumber;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
@Setter
public class CardTransaction {

	private Integer transactionId;
	private String cardUniqueNumber ;
	private Integer amount;
	private String benefitType;
	private Integer benefitAmount;
	private LocalDateTime createdAt;
	private String merchantName;
	private String approvalCode;

}