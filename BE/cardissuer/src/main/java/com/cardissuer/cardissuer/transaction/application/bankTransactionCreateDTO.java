package com.cardissuer.cardissuer.transaction.application;

import java.sql.Timestamp;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class bankTransactionCreateDTO {
	private String accountNumber;
	private Integer amount;
	private String userId;
	private String type;
	private String description;
	private Timestamp createdAt;
}
