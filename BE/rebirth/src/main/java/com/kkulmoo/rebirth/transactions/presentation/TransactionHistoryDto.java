package com.kkulmoo.rebirth.transactions.presentation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionHistoryDto {
    private LocalDateTime transactionDate;
    private String transactionCategoryName;
    private Integer spendingAmount;
    private String merchantName;
    private Integer receivedBenefitAmount;
}