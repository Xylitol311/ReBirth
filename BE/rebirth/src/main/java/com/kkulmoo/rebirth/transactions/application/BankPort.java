package com.kkulmoo.rebirth.transactions.application;

import com.kkulmoo.rebirth.transactions.application.dto.BankTransactionRequest;
import com.kkulmoo.rebirth.transactions.infrastructure.adapter.dto.BankTransactionResponse;
import com.kkulmoo.rebirth.transactions.infrastructure.adapter.dto.UserCIDTO;
import com.kkulmoo.rebirth.user.presentation.requestDTO.UserCIRequest;
import reactor.core.publisher.Mono;

import java.util.List;

public interface BankPort {
    Mono<List<BankTransactionResponse>> getBankTransaction(BankTransactionRequest bankTransactionRequest);
    Mono<List<String>> getAccountNumbersByUserCI(String userCI);

    Mono<UserCIDTO> getUserCI(UserCIRequest userCIRequest);
}
