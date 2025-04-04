package com.kkulmoo.rebirth.payment.infrastructure.repository;


import com.kkulmoo.rebirth.payment.domain.PaymentCard;
import com.kkulmoo.rebirth.payment.domain.repository.CardsRepository;
import com.kkulmoo.rebirth.payment.infrastructure.mapper.PaymentCardEntityMapper;
import com.kkulmoo.rebirth.shared.entity.CardEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.NoSuchElementException;

@Repository
public class CardsRepositoryImpl implements CardsRepository{

    private final CardsJpaRepository cardsJpaRepository;
    private final PaymentCardEntityMapper paymentCardEntityMapper;

    public CardsRepositoryImpl(CardsJpaRepository cardsJpaRepository, PaymentCardEntityMapper paymentCardEntityMapper) {
        this.cardsJpaRepository = cardsJpaRepository;
        this.paymentCardEntityMapper = paymentCardEntityMapper;
    }


    //사용자 보유 카드 리스트 가져오기
    @Override
    public List<PaymentCard> findByUserId(int userId) {

        List<CardEntity> cardsEntity = cardsJpaRepository.findByUserId(userId);
        if(cardsEntity.isEmpty()) return null;

        List<PaymentCard> cards = paymentCardEntityMapper.toCardsList(cardsEntity);
        return cards;
    }

    @Override
    public int findCardTemplateIdByToken(String permanentToken) {
        return cardsJpaRepository.findCardTemplateIdByPermanentToken(permanentToken)
                .orElseThrow(() -> new NoSuchElementException("해당 permanentToken을 가진 카드가 없습니다."));
    }

}
