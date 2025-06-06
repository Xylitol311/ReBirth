package com.cardissuer.cardissuer.transaction.infrastrucuture;
import com.cardissuer.cardissuer.transaction.domain.CardTransaction;
import com.cardissuer.cardissuer.transaction.domain.CardTransactionRepository;
import com.cardissuer.cardissuer.transaction.infrastrucuture.entity.CardTransactionEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
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
			LocalDateTime fromDate) {
		List<CardTransactionEntity> entities = cardTransactionJpaRepository
				.findByCardUniqueNumberAndCreatedAtAfterOrderByCreatedAtDesc(
						cardUniqueNumber, fromDate);

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
