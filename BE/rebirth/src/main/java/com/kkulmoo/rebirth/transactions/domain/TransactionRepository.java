package com.kkulmoo.rebirth.transactions.domain;

import com.kkulmoo.rebirth.transactions.application.dto.CardTransactionQueryParams;
import com.kkulmoo.rebirth.transactions.application.dto.CardTransactionResponse;
import com.kkulmoo.rebirth.transactions.infrastructure.adapter.dto.BankTransactionResponse;
import com.kkulmoo.rebirth.transactions.presentation.TransactionHistoryDto;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface TransactionRepository {
    void saveAllCardTransactions(List<CardTransactionResponse> transactionResponse);
    void saveAllBankTransactions(List<BankTransactionResponse> transactionResponse);
    Slice<TransactionHistoryDto> getCardTransactionHistoryByCardId(CardTransactionQueryParams params);
}
