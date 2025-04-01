package com.kkulmoo.rebirth.transactions.infrastructure.repository;

import com.kkulmoo.rebirth.transactions.application.dto.CardTransactionResponse;
import com.kkulmoo.rebirth.transactions.domain.TransactionRepository;
import com.kkulmoo.rebirth.transactions.infrastructure.entity.TransactionEntity;
import com.kkulmoo.rebirth.transactions.infrastructure.repository.mapper.CardTransactionResponseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TransactionsRepositoryImpl implements TransactionRepository {
    private final BankTransactionsJpaRepository bankTransactionsJpaRepository;
    private final CardTransactionsJpaRepository cardTransactionsJpaRepository;
    private final TransactionsJpaRepository transactionsJpaRepository;
    private final CardTransactionResponseMapper responseMapper;

    @Override
    public void saveAllCardTransactions(List<CardTransactionResponse> transactionResponses) {

        if (transactionResponses == null || transactionResponses.isEmpty()) {
            return;
        }

        List<TransactionEntity> transactionEntities = transactionsJpaRepository.saveAll(
                responseMapper.toTransactionEntities(transactionResponses)
        );

        cardTransactionsJpaRepository.saveAll(
                responseMapper.toCardTransactionEntities(transactionResponses,transactionEntities)
        );
    }

}

