package com.kkulmoo.rebirth.payment.presentation.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class PermanentTokenRequestToCardsaDTO {
    String userCI;
    String cardNumber;
    String password;
    String cvc;
}
