package com.cardissuer.cardissuer.transaction.infrastrucuture.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.cardissuer.cardissuer.transaction.domain.CardTransaction;
import com.cardissuer.cardissuer.transaction.infrastrucuture.CardTransactionEntity;

public class CardTransactionMapper {

	/**
	 * Entity를 Domain 모델로 변환
	 * @param entity CardTransactionEntity
	 * @return CardTransaction 도메인 모델
	 */
	public static CardTransaction toDomain(CardTransactionEntity entity) {
		if (entity == null) {
			return null;
		}

		return CardTransaction.builder()
			.transactionId(entity.getTransactionId())
			.cardUniqueNumber(entity.getCardUniqueNumber())
			.amount(entity.getAmount())
			.createdAt(entity.getCreatedAt())
			.merchantName(entity.getMerchantName())
			.build();
	}

	/**
	 * Domain 모델을 Entity로 변환
	 * @param domain CardTransaction 도메인 모델
	 * @return CardTransactionEntity
	 */
	public static CardTransactionEntity toEntity(CardTransaction domain) {
		if (domain == null) {
			return null;
		}

		return CardTransactionEntity.builder()
			.transactionId(domain.getTransactionId())
			.cardUniqueNumber(domain.getCardUniqueNumber())
			.amount(domain.getAmount())
			.createdAt(domain.getCreatedAt())
			.merchantName(domain.getMerchantName())
			.build();
	}

	/**
	 * Entity 리스트를 Domain 모델 리스트로 변환
	 * @param entities CardTransactionEntity 리스트
	 * @return CardTransaction 도메인 모델 리스트
	 */
	public static List<CardTransaction> toDomainList(List<CardTransactionEntity> entities) {
		if (entities == null) {
			return null;
		}

		return entities.stream()
			.map(CardTransactionMapper::toDomain)
			.collect(Collectors.toList());
	}

	/**
	 * Domain 모델 리스트를 Entity 리스트로 변환
	 * @param domains CardTransaction 도메인 모델 리스트
	 * @return CardTransactionEntity 리스트
	 */
	public static List<CardTransactionEntity> toEntityList(List<CardTransaction> domains) {
		if (domains == null) {
			return null;
		}

		return domains.stream()
			.map(CardTransactionMapper::toEntity)
			.collect(Collectors.toList());
	}
}