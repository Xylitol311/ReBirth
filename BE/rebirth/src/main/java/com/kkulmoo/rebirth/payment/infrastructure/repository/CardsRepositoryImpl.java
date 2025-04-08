package com.kkulmoo.rebirth.payment.infrastructure.repository;


import com.kkulmoo.rebirth.payment.domain.PaymentCard;
import com.kkulmoo.rebirth.payment.domain.repository.CardsRepository;
import com.kkulmoo.rebirth.payment.infrastructure.mapper.PaymentCardEntityMapper;
import com.kkulmoo.rebirth.payment.presentation.response.PermanentTokenResponseByCardsaDTO;
import com.kkulmoo.rebirth.shared.entity.CardEntity;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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

    //사용자 결제 카드 등록

    // cardUniqueId로 검색해서 가져오기


    // 업데이트 해주기


    //사용자 보유 카드 리스트 가져오기
    @Override
    public List<PaymentCard> findByUserId(int userId) {

        List<CardEntity> cardsEntity = cardsJpaRepository.findByUserId(userId);
        if(cardsEntity.isEmpty()) return null;

        List<PaymentCard> cards = paymentCardEntityMapper.toCardsList(cardsEntity);
        return cards;
    }

    //카드 아이디로 카드 가져오기
    @Override
    public PaymentCard findByUserIdAndCardUniqueNumber(Integer userId, String cardUniqueNumber) {
        CardEntity card = cardsJpaRepository.findByUserIdAndCardUniqueNumber(userId,cardUniqueNumber);

        return paymentCardEntityMapper.toCards(card);
    }

    @Override
    public void savePermanentToken(Integer userId, PermanentTokenResponseByCardsaDTO permanentTokenResponseByCardsaDTO, PaymentCard paymentCard) {
        CardEntity card = cardsJpaRepository.findByUserIdAndCardUniqueNumber(userId,paymentCard.getCardUniqueNumber());
        if(card == null) return;
        List<CardEntity> paymentCardList = cardsJpaRepository.findByUserIdAndPaymentCardOrderIsNotNull(card.getUserId());

        CardEntity payCard = card.toBuilder()
                .permanentToken(paymentCard.getPermanentToken())
                .paymentCardOrder((short) (paymentCardList.size()+1)) // 계산해야함
                .paymentCreatedAt(LocalDateTime.now()) // 현재 시간으로
                .build();

        cardsJpaRepository.save(payCard);

    }


}
