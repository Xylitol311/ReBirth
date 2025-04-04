package com.kkulmoo.rebirth.transactions.application;

import com.kkulmoo.rebirth.transactions.application.dto.BankTransactionRequest;
import com.kkulmoo.rebirth.transactions.infrastructure.adapter.dto.BankTransactionResponse;
import reactor.core.publisher.Mono;

import java.util.List;

public interface BankPort {
    Mono<List<BankTransactionResponse>> getBankTransaction(BankTransactionRequest bankTransactionRequest);
    Mono<List<String>> getAccountNumbersByUserCI(String userCI);
}
