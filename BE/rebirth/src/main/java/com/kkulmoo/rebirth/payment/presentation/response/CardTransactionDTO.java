package com.kkulmoo.rebirth.payment.presentation.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CardTransactionDTO {

    LocalDateTime createdAt;
    String response;

}