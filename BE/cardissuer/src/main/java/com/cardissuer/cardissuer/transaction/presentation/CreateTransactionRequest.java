package com.cardissuer.cardissuer.transaction.presentation;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateTransactionRequest {
	String token;
	Integer amount;
	String merchantName;
	LocalDateTime createdAt;
}
