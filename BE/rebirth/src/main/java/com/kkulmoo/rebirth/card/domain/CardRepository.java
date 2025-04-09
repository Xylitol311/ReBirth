package com.kkulmoo.rebirth.card.domain;

import com.kkulmoo.rebirth.shared.entity.CardTemplateEntity;
import com.kkulmoo.rebirth.user.domain.UserId;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CardRepository {
    Optional<CardTemplateEntity> findCardTemplateEntityById(Integer templateId);

    MyCard save(MyCard myCard);

    Optional<MyCard> findById(Integer cardId);

    List<MyCard> findByUserId(UserId userId);

    List<MyCard> findByCardUniqueNumbers(List<String> cardUniqueNumbers, Integer userId);

    Optional<MyCard> findByCardUniqueNumber(String cardUniqueNumber);

    Optional<CardTemplate> findCardTemplateByCardName(String cardName);

    List<MyCard> findByUserIdAndCardIdIn(Integer userId, List<Integer> cardIds);

    void saveAll(Collection<MyCard> cards);

    Integer countByUserId(UserId userId);

    Optional<MyCard> findByPermanentToken(String permanentToken);

    Optional<MyCard> findCardByPermanentTokenAndUserId(String permanentToken, Integer userId);

}