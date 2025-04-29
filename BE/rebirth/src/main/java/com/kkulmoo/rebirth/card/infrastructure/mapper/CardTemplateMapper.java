package com.kkulmoo.rebirth.card.infrastructure.mapper;

import com.kkulmoo.rebirth.card.domain.CardTemplate;
import com.kkulmoo.rebirth.card.domain.CardType;
import com.kkulmoo.rebirth.shared.entity.CardTemplateEntity;
import org.springframework.stereotype.Component;

@Component
public class CardTemplateMapper {

    /**
     * CardTemplateEntity를 CardTemplate 도메인 객체로 변환합니다.
     * @param entity 변환할 엔티티
     * @return 변환된 도메인 객체
     */
    public CardTemplate toDomain(CardTemplateEntity entity) {
        if (entity == null) {
            return null;
        }

        return CardTemplate.builder()
                .cardTemplateId(entity.getCardTemplateId())
                .cardCompanyId(entity.getCardCompanyId())
                .cardName(entity.getCardName())
                .cardImgUrl(entity.getCardImgUrl())
                .annualFee(entity.getAnnualFee())
                .cardType(entity.getCardType().name())
                .cardDetailInfo(entity.getCardDetailInfo())
                .cardConstellationInfo(entity.getCardConstellationInfo())
                .performanceRange(entity.getPerformanceRange())
                .build();
    }

    /**
     * CardTemplate 도메인 객체를 CardTemplateEntity로 변환합니다.
     * @param domain 변환할 도메인 객체
     * @return 변환된 엔티티
     */
    public CardTemplateEntity toEntity(CardTemplate domain) {
        if (domain == null) {
            return null;
        }

        return CardTemplateEntity.builder()
                .cardTemplateId(domain.getCardTemplateId())
                .cardCompanyId(domain.getCardCompanyId())
                .cardName(domain.getCardName())
                .cardImgUrl(domain.getCardImgUrl())
                .annualFee(domain.getAnnualFee())
                .cardType(CardType.valueOf(domain.getCardType()))
                .cardDetailInfo(domain.getCardDetailInfo())
                .cardConstellationInfo(domain.getCardConstellationInfo())
                .performanceRange(domain.getPerformanceRange())
                .build();
    }
}