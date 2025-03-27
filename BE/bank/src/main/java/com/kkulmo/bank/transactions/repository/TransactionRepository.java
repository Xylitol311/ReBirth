package com.kkulmo.bank.transactions.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Integer> {

	List<TransactionEntity> findByAccountNumberAndCreatedAtAfterOrderByCreatedAtDesc(
		String accountNumber, LocalDateTime timestamp);

}