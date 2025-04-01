package com.kkulmoo.rebirth.transactions.domain;

import com.kkulmoo.rebirth.transactions.application.dto.CardTransactionResponse;

import java.util.List;

public interface TransactionRepository {
    void saveAllCardTransactions(List<CardTransactionResponse> transactionResponse);
}
