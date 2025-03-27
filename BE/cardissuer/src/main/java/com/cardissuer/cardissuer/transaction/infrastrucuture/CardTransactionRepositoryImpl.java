package com.cardissuer.cardissuer.transaction.infrastrucuture;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.cardissuer.cardissuer.transaction.domain.CardTransaction;
import com.cardissuer.cardissuer.transaction.domain.CardTransactionRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CardTransactionRepositoryImpl implements CardTransactionRepository {

	private final CardTransactionJpaRepository cardTransactionJpaRepository;
	private final CardTransactionMapper cardTransactionMapper;

	@Override
	public List<CardTransactionEntity> findByCard_UserCIAndCreatedAtAfterOrderByCreatedAtDesc(String userId,
		Timestamp timestamp) {
		return cardTransactionJpaRepository.findByCard_UserCIAndCreatedAtAfterOrderByCreatedAtDesc(userId, timestamp);

	}

	@Override
	public CardTransaction save(CardTransaction cardTransaction) {
		CardTransactionEntity savedEntity = cardTransactionJpaRepository.save(cardTransactionMapper.toEntity(cardTransaction));
		return cardTransactionMapper.toDomain(savedEntity);
	}
}
