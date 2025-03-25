package com.kkulmoo.rebirth.payment.infrastructure.repository;


import com.kkulmoo.rebirth.payment.domain.Cards;
import com.kkulmoo.rebirth.payment.domain.CardsRepository;
import com.kkulmoo.rebirth.payment.infrastructure.entity.CardsEntity;
import com.kkulmoo.rebirth.payment.infrastructure.mapper.CardsEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CardsRespositoryImpl implements CardsRepository {

    private final CardsRepository cardsRepository;
    private final CardsJpaRepository cardsJpaRepository;
    private final CardsEntityMapper cardsEntityMapper;


    //사용자 보유 카드 리스트 가져오기
    @Override
    public List<Cards> findByUserId(int userId) {

        List<CardsEntity> cardsEntity = cardsJpaRepository.findByUserId(userId);
        if(cardsEntity.isEmpty()) return null;

        List<Cards> cards = cardsEntityMapper.toCardsList(cardsEntity);
        return cards;
    }
}
