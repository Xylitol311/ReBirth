package com.kkulmoo.rebirth.transactions.application.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Builder
@Getter
@ToString
public class CardTransactionSingleRequest {
    private String userCI;
    private String cardUniqueNumber;
    private LocalDateTime fromDate;
}
