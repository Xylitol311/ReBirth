package com.kkulmoo.rebirth.payment.presentation.request;

import lombok.Builder;
import lombok.Getter;

//결제시 카드사에게 보내는 정보
@Getter
@Builder
public class CreateTransactionRequestDTO {
    String token;
    int amount;
    String merchantName;
}