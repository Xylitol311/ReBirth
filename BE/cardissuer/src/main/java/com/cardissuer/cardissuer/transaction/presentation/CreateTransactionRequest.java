package com.cardissuer.cardissuer.transaction.presentation;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateTransactionRequest {
	String Token;
	Integer amount;
	String merchantName;
}
