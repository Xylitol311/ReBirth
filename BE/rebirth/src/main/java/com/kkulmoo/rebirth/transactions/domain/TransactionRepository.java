package com.kkulmoo.rebirth.transactions.domain;

import com.kkulmoo.rebirth.transactions.application.dto.CardTransactionResponse;
import com.kkulmoo.rebirth.transactions.infrastructure.adapter.dto.BankTransactionResponse;

import java.util.List;

public interface TransactionRepository {
    void saveAllCardTransactions(List<CardTransactionResponse> transactionResponse);
    void saveAllBankTransactions(List<BankTransactionResponse> transactionResponse);
}
