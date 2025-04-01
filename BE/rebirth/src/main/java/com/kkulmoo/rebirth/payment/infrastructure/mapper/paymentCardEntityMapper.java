package com.kkulmoo.rebirth.payment.infrastructure.mapper;

import com.kkulmoo.rebirth.payment.domain.paymentCard;
import com.kkulmoo.rebirth.shared.entity.CardEntity;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class paymentCardEntityMapper {


    public paymentCard toCards(CardEntity cardsEntity) {

        if (cardsEntity == null) {
            return null;
        }
        return paymentCard.builder()
                .cardId(cardsEntity.getCardId())
                .cardOrder(cardsEntity.getCardOrder())
                .cardTemplateId(cardsEntity.getCardTemplateId())
                .cardUniqueNumber(cardsEntity.getCardUniqueNumber())
                .userId(cardsEntity.getUserId())
                .expiryDate(cardsEntity.getExpiryDate())
                .createdAt(cardsEntity.getCreatedAt())
                .deletedAt(cardsEntity.getDeletedAt())
                .isExpired(cardsEntity.getIsExpired())
                .annualFee(cardsEntity.getAnnualFee())
                .permanentToken(cardsEntity.getPermanentToken())
                .paymentCardOrder(cardsEntity.getPaymentCardOrder())
                .paymentCreatedAt(cardsEntity.getPaymentCreatedAt()).build();
    }

//    public CardsEntity toCardsEntity(){
//
//
//    }

    public List<paymentCard> toCardsList(List<CardEntity> cardsEntities) {

        if (cardsEntities == null) {
            return Collections.emptyList();
        }

        return cardsEntities.stream()
                .map(this::toCards)
                .collect(Collectors.toList());
    }

}



