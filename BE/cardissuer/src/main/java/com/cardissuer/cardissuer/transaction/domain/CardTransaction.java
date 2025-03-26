package com.cardissuer.cardissuer.transaction.domain;

import java.time.LocalDateTime;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CardTransaction {
	private Integer transactionId;
	private String cardUniqueNumber;
	private Integer amount;
	private LocalDateTime createdAt;
	private String merchantName;
}