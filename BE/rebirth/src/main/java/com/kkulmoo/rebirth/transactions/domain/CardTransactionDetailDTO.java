package com.kkulmoo.rebirth.transactions.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CardTransactionDetailDTO {
    private Integer transactionId;
    private LocalDateTime createdAt;
    private String categoryName;
    private String merchantName;
    private Integer amount;
    private Integer benefitAmount;
    private String status;
}