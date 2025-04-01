package com.cardissuer.cardissuer.transaction.infrastrucuture;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cardissuer.cardissuer.transaction.infrastrucuture.CardTransactionEntity;

@Repository
public interface CardTransactionJpaRepository extends JpaRepository<CardTransactionEntity, Integer> {

	List<CardTransactionEntity> findByCardUniqueNumberAndCreatedAtAfterOrderByCreatedAtDesc(
			String cardUniqueNumber,
			Timestamp timestamp
	);

}
