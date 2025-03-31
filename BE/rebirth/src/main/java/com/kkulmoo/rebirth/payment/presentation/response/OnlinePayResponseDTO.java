package com.kkulmoo.rebirth.payment.presentation.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnlinePayResponseDTO {

    String merchantName;
    int amount;
    PaymentTokenResponseDTO paymentTokenResponseDTO;

}
