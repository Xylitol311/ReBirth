package com.cardissuer.cardissuer.transaction.presentation;

import java.sql.Timestamp;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateTransactionRequest {
	String token;
	Integer amount;
	String merchantName;
	Timestamp createdAt;
}
