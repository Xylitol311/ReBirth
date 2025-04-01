package com.kkulmoo.rebirth.payment.infrastructure.repository;


import com.kkulmoo.rebirth.payment.domain.paymentCard;
import com.kkulmoo.rebirth.payment.domain.repository.CardsRepository;
import com.kkulmoo.rebirth.payment.infrastructure.mapper.paymentCardEntityMapper;
import com.kkulmoo.rebirth.shared.entity.CardEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.NoSuchElementException;

@Repository
public class CardsRepositoryImpl implements CardsRepository{

    private final CardsJpaRepository cardsJpaRepository;
    private final paymentCardEntityMapper paymentCardEntityMapper;

    public CardsRepositoryImpl(CardsJpaRepository cardsJpaRepository, paymentCardEntityMapper paymentCardEntityMapper) {
        this.cardsJpaRepository = cardsJpaRepository;
        this.paymentCardEntityMapper = paymentCardEntityMapper;
    }


    //사용자 보유 카드 리스트 가져오기
    @Override
    public List<paymentCard> findByUserId(int userId) {

        List<CardEntity> cardsEntity = cardsJpaRepository.findByUserId(userId);
        if(cardsEntity.isEmpty()) return null;

        List<paymentCard> cards = paymentCardEntityMapper.toCardsList(cardsEntity);
        return cards;
    }

    @Override
    public int findCardTemplateIdByToken(String permanentToken) {
        return cardsJpaRepository.findCardTemplateIdByPermanentToken(permanentToken)
                .orElseThrow(() -> new NoSuchElementException("해당 permanentToken을 가진 카드가 없습니다."));
    }

}
