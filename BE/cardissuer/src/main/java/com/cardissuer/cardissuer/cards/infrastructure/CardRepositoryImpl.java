package com.cardissuer.cardissuer.cards.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.cardissuer.cardissuer.cards.domain.CardRepository;
import com.cardissuer.cardissuer.cards.domain.Card;
import com.cardissuer.cardissuer.cards.domain.CardUniqueNumber;
import com.cardissuer.cardissuer.cards.domain.PermanentToken;
import com.cardissuer.cardissuer.cards.infrastructure.mapper.CardEntityMapper;
import com.cardissuer.cardissuer.cards.infrastructure.mapper.PermanentTokenEntityMapper;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class CardRepositoryImpl implements CardRepository {
	private final CardJpaRepository cardJpaRepository;
	private final CardEntityMapper cardEntityMapper;
	private final TokenJpaRepository tokenJpaRepository;
	private final PermanentTokenEntityMapper permanentTokenEntityMapper;


	@Override
	public List<Card> findByUserIdAndDeletedAtIsNull(Integer userId) {
		// JPA 레포지토리를 통해 CardEntity 목록을 가져옵니다
		List<CardEntity> cardEntities = cardJpaRepository.findByUserIdAndDeletedAtIsNull(userId);

		return cardEntities.stream()
			.map(cardEntityMapper::toDomain)
			.collect(Collectors.toList());
	}

	@Override
	public Optional<Card> findByCardUniqueNumber(CardUniqueNumber cardUniqueNumber) {
		Optional<CardEntity> cardEntity = cardJpaRepository.findById(cardUniqueNumber.getValue());
		return cardEntity.map(cardEntityMapper::toDomain);
	}

	@Override
	public void updateCard(Card card) {
		cardJpaRepository.save(cardEntityMapper.toEntity(card));
	}

	// ToDo: IsAlive인지 체크해야할지말지 고민.
	@Override
	public Optional<PermanentToken> findTokenByCardUniqueNumber(String cardUniqueNumber) {
		Optional<PermanentTokenEntity> entity = tokenJpaRepository.findByCardUniqueNumberAndIsActiveTrue(cardUniqueNumber);
		return entity.map(permanentTokenEntityMapper::toDomain); // Mapper를 사용하여 변환
	}

	@Override
	public Optional<PermanentToken> findTokenByToken(String token) {
		Optional<PermanentTokenEntity> entity = tokenJpaRepository.findById(token);
		return entity.map(permanentTokenEntityMapper::toDomain); // Mapper를 사용하여 변환
	}

	@Override
	public void updateToken(PermanentToken permanentToken) {
		PermanentTokenEntity permanentTokenEntity = permanentTokenEntityMapper.toEntity(permanentToken);
		tokenJpaRepository.save(permanentTokenEntity);
	}

}
