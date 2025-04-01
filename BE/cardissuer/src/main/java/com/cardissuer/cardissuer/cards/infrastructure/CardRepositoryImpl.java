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

	public List<Card> findByUserCIAndDeletedAtIsNull(String userCI) {
		// JPA 레포지토리를 통해 CardEntity 목록을 가져옵니다
		List<CardEntity> cardEntities = cardJpaRepository.findByUserCIAndDeletedAtIsNull(userCI);

		return cardEntities.stream()
			.map(cardEntityMapper::toDomain)
			.collect(Collectors.toList());
	}

	@Override
	public Optional<Card> findByCardUniqueNumber(CardUniqueNumber cardUniqueNumber) {
		System.out.println(cardUniqueNumber.getValue());
		System.out.println("다들어오고 있어요");
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
		System.out.println(token);
		Optional<PermanentTokenEntity> entity = tokenJpaRepository.findById(token);
		return entity.map(permanentTokenEntityMapper::toDomain); // Mapper를 사용하여 변환
	}

	@Override
	public void updateToken(PermanentToken permanentToken) {
		PermanentTokenEntity permanentTokenEntity = permanentTokenEntityMapper.toEntity(permanentToken);
		tokenJpaRepository.save(permanentTokenEntity);
	}

	@Override
	public Card save(Card card) {
		// Card 도메인 객체를 CardEntity로 변환
		// JPA 레포지토리를 통해 엔티티 저장
		// 저장된 엔티티를 다시 도메인 객체로 변환하여 반환
		System.out.println(card.getAccountNumber());
		return cardEntityMapper.toDomain(cardJpaRepository.save(cardEntityMapper.toEntity(card)));
	}

	@Override
	public Boolean existsByCardUniqueNumberAndUserCI(String cardUniqueNumber, String userCI) {
		return  cardJpaRepository.existsByCardUniqueNumberAndUserCI(cardUniqueNumber,userCI);
	}
}

