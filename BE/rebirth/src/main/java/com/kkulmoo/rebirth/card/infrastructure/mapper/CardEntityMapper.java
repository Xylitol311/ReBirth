package com.kkulmoo.rebirth.card.infrastructure.mapper;

import com.kkulmoo.rebirth.card.domain.MyCards;
import com.kkulmoo.rebirth.shared.entity.CardEntity;
import com.kkulmoo.rebirth.user.domain.UserId;
import org.springframework.stereotype.Component;

/**
 * CardEntityMapper - Card와 CardEntity 간의 변환을 담당하는 매퍼 클래스
 */
@Component
public class CardEntityMapper {

    /**
     * CardEntity를 Card로 변환
     *
     * @param entity 변환할 CardEntity 객체
     * @return 변환된 Card 객체
     */
    public MyCards toCard(CardEntity entity) {
        if (entity == null) {
            return null;
        }

        return MyCards.builder()
                .cardId(entity.getCardId())
                .userId(new UserId(entity.getUserId()))
                .cardTemplateId(entity.getCardTemplateId())
                .cardUniqueNumber(entity.getCardUniqueNumber())
                .expiryDate(entity.getExpiryDate())
                .cardOrder(entity.getCardOrder())
                .createdAt(entity.getCreatedAt())
                .deletedAt(entity.getDeletedAt())
                .isExpired(entity.getIsExpired())
                .annualFee(entity.getAnnualFee())
                .cardName(entity.getCardName())
                .spendingTier(entity.getSpendingTier())
                .permanentToken(entity.getPermanentToken())
                .paymentCardOrder(entity.getPaymentCardOrder())
                .paymentCreatedAt(entity.getPaymentCreatedAt())
                .latestLoadDataAt(entity.getLatestLoadDataAt())
                .build();
    }

    /**
     * Card를 CardEntity로 변환
     * 현재 구현에서는 생성자가 필요합니다.
     *
     * @param myCards 변환할 Card 객체
     * @return 변환된 CardEntity 객체
     */
    public CardEntity toEntity(MyCards myCards) {
        if (myCards == null) {
            return null;
        }

        return CardEntity.builder()
                .cardId(myCards.getCardId())
                .userId(myCards.getUserId().getValue())
                .cardTemplateId(myCards.getCardTemplateId())
                .cardUniqueNumber(myCards.getCardUniqueNumber())
                .expiryDate(myCards.getExpiryDate())
                .cardOrder(myCards.getCardOrder())
                .createdAt(myCards.getCreatedAt())
                .deletedAt(myCards.getDeletedAt())
                .cardName(myCards.getCardName())
                .isExpired(myCards.getIsExpired())
                .annualFee(myCards.getAnnualFee())
                .spendingTier(myCards.getSpendingTier())
                .permanentToken(myCards.getPermanentToken())
                .paymentCardOrder(myCards.getPaymentCardOrder())
                .paymentCreatedAt(myCards.getPaymentCreatedAt())
                .latestLoadDataAt(myCards.getLatestLoadDataAt())
                .build();
    }
}