package com.kkulmoo.rebirth.transactions.infrastructure.repository;

import com.kkulmoo.rebirth.transactions.infrastructure.entity.BankTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BankTransactionsJpaRepository extends JpaRepository<BankTransactionEntity,Integer> {
}
