package com.kkulmoo.rebirth.payment.presentation.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateTransactionRequestDTO {
    String token;
    int amount;
    String merchantName;
}