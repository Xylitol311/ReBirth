package com.cardissuer.cardissuer.transaction.domain;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import com.cardissuer.cardissuer.cards.domain.CardUniqueNumber;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CardTransaction {
	private Integer transactionId;
	private CardUniqueNumber cardUniqueNumber;
	private String accountNumber;
	private Integer amount;
	private Timestamp createdAt;
	private String merchantName;
	private String approvalCode;
	private String description;

}