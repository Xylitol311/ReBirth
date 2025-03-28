package com.cardissuer.cardissuer.transaction.application;

import java.sql.Timestamp;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CardTransactionResponse {
	private Timestamp createdAt;
	private String approvalCode;
}