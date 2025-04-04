package com.kkulmoo.rebirth.transactions.infrastructure.repository;

import com.kkulmoo.rebirth.transactions.application.dto.CardTransactionQueryParams;
import com.kkulmoo.rebirth.transactions.application.dto.CardTransactionResponse;
import com.kkulmoo.rebirth.transactions.domain.TransactionRepository;
import com.kkulmoo.rebirth.transactions.infrastructure.adapter.dto.BankTransactionResponse;
import com.kkulmoo.rebirth.transactions.infrastructure.entity.TransactionEntity;
import com.kkulmoo.rebirth.transactions.infrastructure.repository.mapper.BankTransactionMapper;
import com.kkulmoo.rebirth.transactions.infrastructure.repository.mapper.CardTransactionResponseMapper;
import com.kkulmoo.rebirth.transactions.presentation.TransactionHistoryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TransactionsRepositoryImpl implements TransactionRepository {
    private final BankTransactionsJpaRepository bankTransactionsJpaRepository;
    private final CardTransactionsJpaRepository cardTransactionsJpaRepository;
    private final TransactionsJpaRepository transactionsJpaRepository;
    private final CardTransactionResponseMapper responseMapper;
    private final BankTransactionMapper bankTransactionMapper;


    @Override
    public Slice<TransactionHistoryDto> getCardTransactionHistoryByCardId(CardTransactionQueryParams params) {
        return transactionsJpaRepository.findTransactionsByUserIdYearMonth(
                params.getCardId(),
                params.getYear(),
                params.getMonth(),
                params.getPageable());
    }

    @Override
    public void saveAllCardTransactions(List<CardTransactionResponse> transactionResponses) {

        if (transactionResponses == null || transactionResponses.isEmpty()) {
            return;
        }

        for (CardTransactionResponse response : transactionResponses) {
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!Transaction: " + response.toString());
        }

        List<TransactionEntity> transactionEntities = transactionsJpaRepository.saveAll(
                responseMapper.toTransactionEntities(transactionResponses)
        );

        cardTransactionsJpaRepository.saveAll(
                responseMapper.toCardTransactionEntities(transactionResponses,transactionEntities)
        );
    }

    @Override
    public void saveAllBankTransactions(List<BankTransactionResponse> transactionResponses) {
        if (transactionResponses == null || transactionResponses.isEmpty()) {
            return;
        }

        // 1. 먼저 TransactionEntity 리스트를 생성하여 저장
        List<TransactionEntity> transactionEntities = transactionsJpaRepository.saveAll(
                bankTransactionMapper.toTransactionEntities(transactionResponses)
        );

        // 2. 저장된 TransactionEntity 리스트를 기반으로 BankTransactionEntity 리스트 생성 및 저장
        bankTransactionsJpaRepository.saveAll(
                bankTransactionMapper.toBankTransactionEntities(transactionResponses, transactionEntities)
        );
    }
}