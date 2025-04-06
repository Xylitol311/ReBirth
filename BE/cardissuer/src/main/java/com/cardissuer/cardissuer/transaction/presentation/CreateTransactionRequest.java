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
	//discount, mileage 2개중 하나
	Integer benefitId;
	String benefitType;
	Integer benefitAmount;
	LocalDateTime createdAt;
}

