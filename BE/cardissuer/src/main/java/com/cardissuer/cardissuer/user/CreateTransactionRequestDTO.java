package com.cardissuer.cardissuer.user;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateTransactionRequestDTO {
    String token;
    int amount;
    String merchantName;
}