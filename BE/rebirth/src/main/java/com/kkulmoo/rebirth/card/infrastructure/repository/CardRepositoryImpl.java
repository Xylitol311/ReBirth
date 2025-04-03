package com.kkulmoo.rebirth.card.infrastructure.repository;

import com.kkulmoo.rebirth.card.domain.CardRepository;
import com.kkulmoo.rebirth.card.domain.CardTemplate;
import com.kkulmoo.rebirth.card.domain.myCard;
import com.kkulmoo.rebirth.card.infrastructure.mapper.CardEntityMapper;
import com.kkulmoo.rebirth.payment.infrastructure.repository.CardTemplateJpaRepository;
import com.kkulmoo.rebirth.shared.entity.CardEntity;
import com.kkulmoo.rebirth.shared.entity.CardTemplateEntity;
import com.kkulmoo.rebirth.user.domain.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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

	@Override
	public Optional<CardTemplateEntity>findCardTemplateEntityById(Integer templateId){
		return cardTemplateJpaRepository.findById(templateId);
	}


	@Override
	public myCard save(myCard myCard) {
		CardEntity cardEntity = cardJpaRepository.save(cardEntityMapper.toEntity(myCard));
		return cardEntityMapper.toCard(cardEntity);
	}

	@Override
	public Optional<myCard> findById(Integer cardId) {
		return cardJpaRepository.findById(cardId)
			.map(cardEntityMapper::toCard);
	}

	@Override
	public List<myCard> findByUserId(UserId userId) {
		if (userId == null) {
			return Collections.emptyList();
		}

		return cardJpaRepository.findByUserId(userId.getValue())
				.stream()
				.map(cardEntityMapper::toCard)
				.collect(Collectors.toList());
	}


	@Override
	public List<myCard> findByCardUniqueNumbers(List<String> cardUniqueNumbers) {
		return cardJpaRepository.findByCardUniqueNumberIn(cardUniqueNumbers)
			.stream()
			.map(cardEntityMapper::toCard)
			.collect(Collectors.toList());
	}

	@Override
	public Optional<myCard> findByCardUniqueNumber(String cardUniqueNumber) {
		return Optional.empty();
	}

	@Override
	public Optional<CardTemplate> findCardTemplateByCardName(String cardName) {
		return null;
	}



}
