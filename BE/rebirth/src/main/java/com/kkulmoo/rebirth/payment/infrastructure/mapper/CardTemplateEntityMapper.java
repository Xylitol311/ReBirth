package com.kkulmoo.rebirth.payment.infrastructure.mapper;

import com.kkulmoo.rebirth.card.domain.CardType;
import com.kkulmoo.rebirth.payment.domain.CardTemplate;
import com.kkulmoo.rebirth.shared.entity.CardTemplateEntity;
import org.springframework.stereotype.Component;

@Component
public class CardTemplateEntityMapper {

    public CardTemplate toCardTemplate(CardTemplateEntity cardTemplateEntity) {

        return CardTemplate.builder()
                .cardTemplateId(cardTemplateEntity.getCardTemplateId())
                .cardCompanyId(cardTemplateEntity.getCardCompanyId())
                .cardName(cardTemplateEntity.getCardName())
                .cardImgUrl(cardTemplateEntity.getCardImgUrl())
                .cardDetailInfo(cardTemplateEntity.getCardDetailInfo())
                .cardConstellationInfo(cardTemplateEntity.getCardConstellationInfo())
                .annualFee(cardTemplateEntity.getAnnualFee())
                .cardType(cardTemplateEntity.getCardType().toString()) // Convert enum to String
                .build();
    }

    public CardTemplateEntity toCardTemplateEntity(CardTemplate cardTemplate) {

        return CardTemplateEntity.builder()
                .cardTemplateId(cardTemplate.getCardTemplateId())
                .cardCompanyId(cardTemplate.getCardCompanyId())
                .cardName(cardTemplate.getCardName())
                .cardImgUrl(cardTemplate.getCardImgUrl())
                .cardDetailInfo(cardTemplate.getCardDetailInfo())
                .cardConstellationInfo(cardTemplate.getCardConstellationInfo())
                .annualFee(cardTemplate.getAnnualFee())
                .cardType(CardType.valueOf(cardTemplate.getCardType())) // Convert String to enum
                .build();
    }
}