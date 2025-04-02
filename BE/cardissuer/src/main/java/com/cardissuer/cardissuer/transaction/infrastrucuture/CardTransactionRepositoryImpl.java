package com.cardissuer.cardissuer.transaction.infrastrucuture;
import com.cardissuer.cardissuer.transaction.domain.CardTransaction;
import com.cardissuer.cardissuer.transaction.domain.CardTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CardTransactionRepositoryImpl implements CardTransactionRepository {

	private final CardTransactionJpaRepository cardTransactionJpaRepository;
	private final CardTransactionMapper cardTransactionMapper;

	@Override
	public List<CardTransaction> findByCardUniqueNumberAndCreatedAtAfterOrderByCreatedAtDesc(
			String cardUniqueNumber,
			Timestamp timestamp) {
		List<com.cardissuer.cardissuer.transaction.infrastrucuture.CardTransactionEntity> entities = cardTransactionJpaRepository
				.findByCardUniqueNumberAndCreatedAtAfterOrderByCreatedAtDesc(
						cardUniqueNumber, timestamp);

		// 엔티티를 도메인 객체로 변환
		return entities.stream()
				.map(cardTransactionMapper::toDomain)
				.collect(Collectors.toList());
	}

	@Override
	public void save(CardTransaction cardTransaction) {
		cardTransactionJpaRepository.save(cardTransactionMapper.toEntity(cardTransaction));
	}
}
