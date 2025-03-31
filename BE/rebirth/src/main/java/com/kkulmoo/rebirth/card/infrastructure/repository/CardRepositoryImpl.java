package com.kkulmoo.rebirth.card.infrastructure.repository;

import com.kkulmoo.rebirth.card.domain.Card;
import com.kkulmoo.rebirth.card.domain.CardRepository;
import com.kkulmoo.rebirth.card.domain.CardTemplate;
import com.kkulmoo.rebirth.card.infrastructure.mapper.CardEntityMapper;
import com.kkulmoo.rebirth.payment.infrastructure.repository.CardTemplateJpaRepository;
import com.kkulmoo.rebirth.shared.entity.CardsEntity;
import com.kkulmoo.rebirth.user.domain.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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

	@Override
	public Card save(Card card) {
		CardsEntity cardEntity = cardJpaRepository.save(cardEntityMapper.toEntity(card));
		return cardEntityMapper.toCard(cardEntity);
	}

	@Override
	public Optional<Card> findById(Integer cardId) {
		return cardJpaRepository.findById(cardId)
			.map(cardEntityMapper::toCard);
	}

	@Override
	public List<Card> findByUserId(UserId userId ) {
		return cardJpaRepository.findByUserId(userId.getValue())
			.stream()
			.map(cardEntityMapper::toCard)
			.collect(Collectors.toList());
	}

	@Override
	public Optional<Card> findByCardUniqueNumber(String cardUniqueNumber) {
		return Optional.empty();
	}

	@Override
	public Optional<CardTemplate> findCardTemplateByCardName(String cardName) {
		return null;
	}

}
