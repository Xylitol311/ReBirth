package com.kkulmoo.rebirth.transactions.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
public class CardTransactionSingleRequest {
    private String userCI;
    private String cardUniqueNumber;
    private LocalDateTime fromDate;
}
