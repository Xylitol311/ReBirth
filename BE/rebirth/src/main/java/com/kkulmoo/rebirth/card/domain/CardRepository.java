package com.kkulmoo.rebirth.card.domain;

import com.kkulmoo.rebirth.payment.infrastructure.dto.MyCardDto;
import com.kkulmoo.rebirth.shared.entity.CardTemplateEntity;
import com.kkulmoo.rebirth.user.domain.UserId;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CardRepository {
    Optional<CardTemplateEntity> findCardTemplateEntityById(Integer templateId);

    MyCards save(MyCards myCards);

    Optional<MyCards> findById(Integer cardId);

    List<MyCards> findByUserId(UserId userId);

    List<MyCards> findByCardUniqueNumbers(List<String> cardUniqueNumbers);
    // 필요한 다른 메서드들

    Optional<MyCards> findByCardUniqueNumber(String cardUniqueNumber);

    Optional<CardTemplate> findCardTemplateByCardName(String cardName);

    List<MyCards> findByUserIdAndCardIdIn(Integer userId, List<Integer> cardIds);

    void saveAll(Collection<MyCards> cards);

    Integer countByUserId(UserId userId);

    MyCardDto findMyCardIdAndTemplateIdByPermanentToken(String permanentToken);

    List<MyCardDto> findMyCardsIdAndTemplateIdsByUserId(Integer userId);
}