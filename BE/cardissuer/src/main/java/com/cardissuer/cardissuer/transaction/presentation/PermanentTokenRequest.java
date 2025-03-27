package com.cardissuer.cardissuer.transaction.presentation;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PermanentTokenRequest {
	String userCI;
	String cardUniqueNumber;
	String cardNumber;
	String password;
	String cvc;
}
