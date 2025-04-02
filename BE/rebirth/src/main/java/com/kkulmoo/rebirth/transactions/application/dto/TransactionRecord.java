package com.kkulmoo.rebirth.transactions.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRecord {
    private LocalDateTime transactionDate;
    private String transactionCategory;
    private Integer spendingAmount;
    private String merchantName;
    private Integer receivedBenefitAmount;
}