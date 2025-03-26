package com.cardissuer.cardissuer.cards.infrastructure.mapper;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.cardissuer.cardissuer.cards.domain.CardUniqueNumber;
import com.cardissuer.cardissuer.cards.infrastructure.CardEntity;
import com.cardissuer.cardissuer.cards.domain.Card;

@Component
public class CardEntityMapper {

	/**
	 * CardEntity를 CardDomainModel로 변환합니다.
	 *
	 * @param entity 변환할 CardEntity 객체
	 * @return 변환된 CardDomainModel 객체
	 */
	public Card toDomain(CardEntity entity) {
		if (entity == null) {
			return null;
		}

		return Card.builder()
			.cardUniqueNumber(CardUniqueNumber.of(entity.getCardUniqueNumber()))
			.userId(entity.getUserId())
			.cardNumber(entity.getCardNumber())
			.cardName(entity.getCardName())
			.expiryDate(entity.getExpiryDate())
			.cvc(entity.getCvc())
			.cardPassword(entity.getCardPassword())
			.createdAt(entity.getCreatedAt())
			.deletedAt(entity.getDeletedAt())
			.build();
	}

	/**
	 * CardDomainModel을 CardEntity로 변환합니다.
	 *
	 * @param domain 변환할 CardDomainModel 객체
	 * @return 변환된 CardEntity 객체
	 */
	public CardEntity toEntity(Card domain) {
		if (domain == null) {
			return null;
		}

		return CardEntity.builder()
			.cardUniqueNumber(domain.getCardUniqueNumber().getValue())
			.userId(domain.getUserId())
			.cardNumber(domain.getCardNumber())
			.cardName(domain.getCardName())
			.expiryDate(domain.getExpiryDate())
			.cvc(domain.getCvc())
			.cardPassword(domain.getCardPassword())
			.createdAt(domain.getCreatedAt())
			.deletedAt(domain.getDeletedAt())
			.build();
	}

	/**
	 * 여러 개의 CardEntity를 CardDomainModel 리스트로 변환합니다.
	 *
	 * @param entities 변환할 CardEntity 리스트
	 * @return 변환된 CardDomainModel 리스트
	 */
	public List<Card> toDomainList(List<CardEntity> entities) {
		if (entities == null) {
			return Collections.emptyList();
		}

		return entities.stream()
			.map(this::toDomain)
			.collect(Collectors.toList());
	}

	/**
	 * 여러 개의 CardDomainModel을 CardEntity 리스트로 변환합니다.
	 *
	 * @param domains 변환할 CardDomainModel 리스트
	 * @return 변환된 CardEntity 리스트
	 */
	public List<CardEntity> toEntityList(List<Card> domains) {
		if (domains == null) {
			return Collections.emptyList();
		}

		return domains.stream()
			.map(this::toEntity)
			.collect(Collectors.toList());
	}
}