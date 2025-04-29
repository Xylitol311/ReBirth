package com.kkulmoo.rebirth.transactions.infrastructure.repository;

import com.kkulmoo.rebirth.transactions.infrastructure.entity.CardTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardTransactionsJpaRepository extends JpaRepository<CardTransactionEntity, Integer> {

}
