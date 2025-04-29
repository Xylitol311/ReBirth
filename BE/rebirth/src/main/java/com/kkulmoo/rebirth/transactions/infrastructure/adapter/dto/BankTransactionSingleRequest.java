package com.kkulmoo.rebirth.transactions.infrastructure.adapter.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BankTransactionSingleRequest {
    private String userCI;
    private String accountNumber;
    private LocalDateTime timestamp;     // 조회 시간 기준
}
