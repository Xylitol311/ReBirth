package com.kkulmoo.rebirth.payment.presentation.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentTokenResponseDTO {
    String token;
    String cardId;
}
