package com.kkulmoo.rebirth.card.infrastructure.mapper;

import com.kkulmoo.rebirth.card.domain.Card;
import com.kkulmoo.rebirth.shared.entity.CardsEntity;
import com.kkulmoo.rebirth.user.domain.UserId;
import org.springframework.stereotype.Component;

/**
 * CardEntityMapper - Card와 CardEntity 간의 변환을 담당하는 매퍼 클래스
 */
@Component
public class CardEntityMapper {

	/**
	 * CardEntity를 Card로 변환
	 * @param entity 변환할 CardEntity 객체
	 * @return 변환된 Card 객체
	 */
	public Card toCard(CardsEntity entity) {
		if (entity == null) {
			return null;
		}

		return Card.builder()
			.cardId(entity.getCardId())
			.userId(new UserId(entity.getUserId()))
			.cardTemplateId(entity.getCardTemplateId())
			.cardNumber(entity.getCardNumber())
			.cardUniqueNumber(entity.getCardUniqueNumber())
			.expiryDate(entity.getExpiryDate())
			.cardOrder(entity.getCardOrder())
			.createdAt(entity.getCreatedAt())
			.deletedAt(entity.getDeletedAt())
			.isExpired(entity.getIsExpired())
			.annualFee(entity.getAnnualFee())
			.permanentToken(entity.getPermanentToken())
			.paymentCardOrder(entity.getPaymentCardOrder())
			.paymentCreatedAt(entity.getPaymentCreatedAt())
			.build();
	}

	/**
	 * Card를 CardEntity로 변환
	 * 현재 구현에서는 생성자가 필요합니다.
	 *
	 * @param card 변환할 Card 객체
	 * @return 변환된 CardEntity 객체
	 */
	public CardsEntity toEntity(Card card) {
		if (card == null) {
			return null;
		}

		return CardsEntity.builder()
			.cardId(card.getCardId())
			.userId(card.getUserId().getValue())
			.cardTemplateId(card.getCardTemplateId())
			.cardNumber(card.getCardNumber())
			.cardUniqueNumber(card.getCardUniqueNumber())
			.expiryDate(card.getExpiryDate())
			.cardOrder(card.getCardOrder())
			.createdAt(card.getCreatedAt())
			.deletedAt(card.getDeletedAt())
			.isExpired(card.getIsExpired())
			.annualFee(card.getAnnualFee())
			.permanentToken(card.getPermanentToken())
			.paymentCardOrder(card.getPaymentCardOrder())
			.paymentCreatedAt(card.getPaymentCreatedAt())
			.build();
	}
}