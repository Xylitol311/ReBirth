package com.cardissuer.cardissuer.user;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateTransactionRequestDTO {
    String Token;
    int amount;
    String merchantName;
}