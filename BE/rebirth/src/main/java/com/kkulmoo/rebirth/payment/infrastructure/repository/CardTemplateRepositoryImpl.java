package com.kkulmoo.rebirth.payment.infrastructure.repository;

import com.kkulmoo.rebirth.payment.domain.CardTemplate;
import com.kkulmoo.rebirth.payment.domain.CardTemplateRepository;
import com.kkulmoo.rebirth.payment.domain.Cards;
import com.kkulmoo.rebirth.payment.infrastructure.entity.CardTemplateEntity;
import com.kkulmoo.rebirth.payment.infrastructure.entity.CardsEntity;
import com.kkulmoo.rebirth.payment.infrastructure.mapper.CardTemplateEntityMapper;

import java.util.List;
import java.util.Optional;

public class CardTemplateRepositoryImpl implements CardTemplateRepository {

    private final CardTemplateJpaRespository cardsTemplateJpaRepository;
    private final CardTemplateEntityMapper cardTemplateEntityMapper;

    public CardTemplateRepositoryImpl(CardTemplateJpaRespository cardsTemplateJpaRepository, CardTemplateEntityMapper cardTemplateEntityMapper) {
        this.cardsTemplateJpaRepository = cardsTemplateJpaRepository;
        this.cardTemplateEntityMapper = cardTemplateEntityMapper;
    }


    @Override
    public CardTemplate getCardTemplate(int cardTemplateId) {

        Optional<CardTemplateEntity> cardTemplateEntity = cardsTemplateJpaRepository.findById(cardTemplateId);
        if(cardTemplateEntity.isEmpty()) return null;

        return cardTemplateEntityMapper.toCardTemplate(cardTemplateEntity.orElseThrow());
    }
}
