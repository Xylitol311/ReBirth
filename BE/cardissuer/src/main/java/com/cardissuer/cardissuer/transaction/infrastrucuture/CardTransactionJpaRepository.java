package com.cardissuer.cardissuer.transaction.infrastrucuture;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import com.cardissuer.cardissuer.transaction.infrastrucuture.entity.CardTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CardTransactionJpaRepository extends JpaRepository<CardTransactionEntity, Integer> {

	List<CardTransactionEntity> findByCardUniqueNumberAndCreatedAtAfterOrderByCreatedAtDesc(
			String cardUniqueNumber,
			LocalDateTime fromDate
	);

}
