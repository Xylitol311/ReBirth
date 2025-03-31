package com.kkulmoo.rebirth.payment.presentation.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnlinePayResponseDTO {

    String merchantName;
    int amount;
    List<PaymentTokenResponseDTO> paymentTokenResponseDTO;

}
