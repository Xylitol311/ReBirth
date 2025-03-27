package com.kkulmoo.rebirth.payment.presentation.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateTransactionRequestDTO {
    String Token;
    int amount;
    String merchantName;
}