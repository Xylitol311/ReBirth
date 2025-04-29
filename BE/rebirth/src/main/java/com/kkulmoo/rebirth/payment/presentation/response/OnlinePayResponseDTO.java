package com.kkulmoo.rebirth.payment.presentation.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnlinePayResponseDTO {

    String merchantName;
    int amount;
    List<PaymentTokenResponseDTO> paymentTokenResponseDTO;

}
