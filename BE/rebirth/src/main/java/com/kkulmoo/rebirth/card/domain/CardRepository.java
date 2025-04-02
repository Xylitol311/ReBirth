package com.kkulmoo.rebirth.card.domain;

import com.kkulmoo.rebirth.shared.entity.CardTemplateEntity;
import com.kkulmoo.rebirth.user.domain.UserId;

import java.util.List;
import java.util.Optional;

public interface CardRepository {
	Optional<CardTemplateEntity>findCardTemplateEntityById(Integer templateId);

	myCard save(myCard myCard);

	Optional<myCard> findById(Integer cardId);

	List<myCard> findByUserId(UserId userId);

	List<myCard> findByCardUniqueNumbers(List<String> cardUniqueNumbers);
	// 필요한 다른 메서드들

	Optional<myCard> findByCardUniqueNumber(String cardUniqueNumber);

	Optional<CardTemplate> findCardTemplateByCardName (String cardName);


}