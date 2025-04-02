package com.kkulmoo.rebirth.transactions.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class BankTransactionRequest {
    private String userCI;
    private List<String> bankAccounts;
    private LocalDateTime timestamp;
}
