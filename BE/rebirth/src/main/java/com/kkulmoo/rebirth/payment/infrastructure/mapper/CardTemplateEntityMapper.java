package com.kkulmoo.rebirth.payment.infrastructure.mapper;

import com.kkulmoo.rebirth.payment.domain.CardTemplate;
import com.kkulmoo.rebirth.payment.domain.Cards;
import com.kkulmoo.rebirth.payment.infrastructure.entity.CardTemplateEntity;
import com.kkulmoo.rebirth.payment.infrastructure.entity.CardsEntity;
import org.springframework.stereotype.Component;

@Component
public class CardTemplateEntityMapper {

    public CardTemplate toCardTemplate(CardTemplateEntity cardTemplateEntity){

        if(cardTemplateEntity == null) {
            return null;
        }

        return CardTemplate.builder()
                .cardTemplateId(cardTemplateEntity.getCardTemplateId())
                .cardCompanyId(cardTemplateEntity.getCardCompanyId())
                .cardName(cardTemplateEntity.getCardName())
                .annualFee(cardTemplateEntity.getAnnualFee())
                .benefitConditions(cardTemplateEntity.getBenefitConditions())
                .lastMonthUsageRanges(cardTemplateEntity.getLastMonthUsageRanges())
                .cardType(cardTemplateEntity.getCardType())
                .cardImgUrl(cardTemplateEntity.getCardImgUrl())
                .godImgUrl(cardTemplateEntity.getGodImgUrl())
                .godName(cardTemplateEntity.getGodName())
                .cardConstellationInfo(cardTemplateEntity.getCardConstellationInfo())
                .cardDeityName(cardTemplateEntity.getCardDeityName())
                .cardDeityImgUrl(cardTemplateEntity.getCardDeityImgUrl())
                .cardDetailInfo(cardTemplateEntity.getCardDetailInfo())
                .build();


    }
}
