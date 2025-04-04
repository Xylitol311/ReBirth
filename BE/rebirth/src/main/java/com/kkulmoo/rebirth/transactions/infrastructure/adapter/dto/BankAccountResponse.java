package com.kkulmoo.rebirth.transactions.infrastructure.adapter.dto;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BankAccountResponse {
    private String accountNumber;
}