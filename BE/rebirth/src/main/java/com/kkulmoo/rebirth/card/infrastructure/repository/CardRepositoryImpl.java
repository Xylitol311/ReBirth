package com.kkulmoo.rebirth.card.infrastructure.repository;

import com.kkulmoo.rebirth.card.domain.CardRepository;
import com.kkulmoo.rebirth.card.domain.CardTemplate;
import com.kkulmoo.rebirth.card.domain.MyCard;
import com.kkulmoo.rebirth.card.infrastructure.mapper.CardEntityMapper;
import com.kkulmoo.rebirth.card.infrastructure.mapper.CardTemplateMapper;
import com.kkulmoo.rebirth.payment.infrastructure.repository.CardTemplateJpaRepository;
import com.kkulmoo.rebirth.shared.entity.CardEntity;
import com.kkulmoo.rebirth.shared.entity.CardTemplateEntity;
import com.kkulmoo.rebirth.user.domain.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// JPA 구현체
@Repository
@RequiredArgsConstructor
public class CardRepositoryImpl implements CardRepository {
    private final CardJpaRepository cardJpaRepository;
    private final CardEntityMapper cardEntityMapper;
    private final CardTemplateJpaRepository cardTemplateJpaRepository;
    private final CardTemplateMapper cardTemplateMapper;

    @Override
    public Optional<CardTemplateEntity> findCardTemplateEntityById(Integer templateId) {
        return cardTemplateJpaRepository.findById(templateId);
    }


    @Override
    public MyCard save(MyCard myCard) {
        CardEntity cardEntity = cardJpaRepository.save(cardEntityMapper.toEntity(myCard));
        return cardEntityMapper.toCard(cardEntity);
    }

    @Override
    public Optional<MyCard> findById(Integer cardId) {
        return cardJpaRepository.findById(cardId)
                .map(cardEntityMapper::toCard);
    }

    @Override
    public List<MyCard> findByUserId(UserId userId) {
        if (userId == null) {
            return Collections.emptyList();
        }

        return cardJpaRepository.findByUserId(userId.getValue())
                .stream()
                .map(cardEntityMapper::toCard)
                .collect(Collectors.toList());
    }


    @Override
    public List<MyCard> findByCardUniqueNumbers(List<String> cardUniqueNumbers, Integer userId) {
        return cardJpaRepository.findByCardUniqueNumberInAndUserId(cardUniqueNumbers, userId)
                .stream()
                .map(cardEntityMapper::toCard)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<MyCard> findByCardUniqueNumber(String cardUniqueNumber) {
        return cardJpaRepository.findByCardUniqueNumber(cardUniqueNumber)
                .map(cardEntityMapper::toCard);
    }

    @Override
    public Optional<CardTemplate> findCardTemplateByCardName(String cardName) {
        return cardTemplateJpaRepository.findByCardName(cardName)
                .map(cardTemplateMapper::toDomain); // Entity를 Domain 객체로 변환
    }

    @Override
    public List<MyCard> findByUserIdAndCardIdIn(Integer userId, List<Integer> cardIds) {
        return cardJpaRepository.findByUserIdAndCardIdIn(userId, cardIds)
                .stream()
                .map(cardEntityMapper::toCard)
                .collect(Collectors.toList());
    }

    @Override
    public void saveAll(Collection<MyCard> cards) {
        List<CardEntity> entities = cards.stream()
                .map(cardEntityMapper::toEntity)
                .collect(Collectors.toList());

        cardJpaRepository.saveAll(entities);
    }

    @Override
    public Integer countByUserId(UserId userId) {
        return cardJpaRepository.countByUserId(userId.getValue());
    }


    @Override
    public Optional<MyCard> findCardByPermanentTokenAndUserId(String permanentToken, Integer userId) {
        return cardJpaRepository.findByPermanentTokenAndUserId(permanentToken, userId)
                .map(cardEntityMapper::toCard);    }

}
