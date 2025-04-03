package com.cardissuer.cardissuer.transaction.domain;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.stereotype.Repository;


@Repository
public interface CardTransactionRepository {

	List<CardTransaction> findByCardUniqueNumberAndCreatedAtAfterOrderByCreatedAtDesc(
			String cardUniqueNumber,
			Timestamp timestamp
	);

	void save (CardTransaction card);
}
