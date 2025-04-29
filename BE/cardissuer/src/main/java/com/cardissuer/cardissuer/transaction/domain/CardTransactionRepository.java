package com.cardissuer.cardissuer.transaction.domain;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;


@Repository
public interface CardTransactionRepository {

	List<CardTransaction> findByCardUniqueNumberAndCreatedAtAfterOrderByCreatedAtDesc(
			String cardUniqueNumber,
			LocalDateTime fromDate
	);

	void save (CardTransaction card);
}
