package com.kkulmoo.rebirth.card.domain;

import java.util.List;
import java.util.Optional;

import com.kkulmoo.rebirth.user.domain.UserId;

public interface CardRepository {
	Card save(Card card);

	Optional<Card> findById(Integer cardId);

	List<Card> findByUserId(UserId userId);
	// 필요한 다른 메서드들

	Optional<Card> findByCardUniqueNumber(String cardUniqueNumber);

	Optional<CardTemplate> findCardTemplateByCardName (String cardName);
}