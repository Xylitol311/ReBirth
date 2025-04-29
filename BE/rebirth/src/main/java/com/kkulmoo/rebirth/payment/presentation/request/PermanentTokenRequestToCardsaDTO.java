package com.kkulmoo.rebirth.payment.presentation.request;

import lombok.*;

@AllArgsConstructor
@Getter
@Builder
@Setter
@ToString
public class PermanentTokenRequestToCardsaDTO {
    String userCI;
    String cardNumber;
    String password;
    String cvc;
}
