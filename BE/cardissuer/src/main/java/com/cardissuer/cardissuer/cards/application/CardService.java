package com.cardissuer.cardissuer.cards.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cardissuer.cardissuer.cards.domain.CardRepository;
import com.cardissuer.cardissuer.cards.domain.Card;
import com.cardissuer.cardissuer.cards.domain.CardUniqueNumber;
import com.cardissuer.cardissuer.cards.domain.PermanentToken;
import com.cardissuer.cardissuer.cards.infrastructure.CardEntity;
import com.cardissuer.cardissuer.cards.infrastructure.PermanentTokenEntity;
import com.cardissuer.cardissuer.cards.infrastructure.mapper.CardEntityMapper;
import com.cardissuer.cardissuer.cards.presentation.CardCreateRequest;
import com.cardissuer.cardissuer.common.exception.NotFoundException;
import com.cardissuer.cardissuer.common.exception.UnauthorizedException;
import com.cardissuer.cardissuer.common.exception.UserNotFoundException;
import com.cardissuer.cardissuer.transaction.presentation.PermanentTokenRequest;
import com.cardissuer.cardissuer.user.domain.User;
import com.cardissuer.cardissuer.user.domain.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CardService {

	// 이렇게 의존성 주입하는건 싫지만 카드사는 귀찮아서.. 그냥 갑니다..
	private final UserRepository userRepository;
	private final CardRepository cardRepository;
	private final CardEntityMapper cardEntityMapper;

	public Card createCard(CardCreateRequest request) {
		// Card 도메인 객체 생성
		Card card = Card.builder()
			.cardUniqueNumber(CardUniqueNumber.of(null))
			.userCI(request.getUserCI())  // 현재 로그인 사용자 정보 가져오기
			.accountNumber(request.getAccountNumber())  // 계좌 정보 가져오기
			.createdAt(request.getCreatedAt())
			.build();

		System.out.println(request.getAccountNumber());
		// 엔티티로 변환하여 저장하고 다시 도메인 객체로 변환
		return cardRepository.save(card);
	}

	// 도메인 객체를 엔티티로 변환
	private CardEntity convertToEntity(Card card) {
		return CardEntity.builder()
			.cardUniqueNumber(card.getCardUniqueNumber().getValue())
			.userCI(card.getUserCI())
			.accountNumber(card.getAccountNumber())
			.cardNumber(card.getCardNumber())
			.cardName(card.getCardName())
			.expiryDate(card.getExpiryDate())
			.cvc(card.getCvc())
			.cardPassword(card.getCardPassword())
			.createdAt(card.getCreatedAt())
			.deletedAt(card.getDeletedAt())
			.build();
	}

	@Transactional
	public PermanentToken getPermanentToken(
		String userAPiKey,
		PermanentTokenRequest permanentTokenRequest) {
		//영구토큰 받기
		CardUniqueNumber cardUniqueNumber = CardUniqueNumber.of(permanentTokenRequest.getCardUniqueNumber());

		//User꺼내기
		Optional<User> optionalUser = userRepository.findByUserApiKey(userAPiKey);
		Optional<Card> optionalCard = cardRepository.findByCardUniqueNumber(
			cardUniqueNumber
		);

		if (!optionalUser.isPresent()) {
			throw new NotFoundException("사용자를 찾을 수 없습니다.");
		}

		if (!optionalCard.isPresent()) {
			throw new NotFoundException("카드를 찾을 수 없습니다.");
		}

		User user = optionalUser.get();
		Card card = optionalCard.get();

		//먼저 해당 유저의 카드가 맞는지.
		if (!user.getUserCI().equals(card.getUserCI())) {
			throw new UnauthorizedException("해당 카드에 접근 권한이 없습니다.");
		}

		Optional<PermanentToken> existingToken = cardRepository.findTokenByCardUniqueNumber(
			permanentTokenRequest.getCardUniqueNumber());

		if (existingToken.isPresent()) {
			return existingToken.get();
		} else {
			// 기존 토큰이 없으면 새 토큰을 생성
			PermanentToken newToken = createNewPermanentToken(card, permanentTokenRequest);
			return newToken;
		}
	}

	public PermanentToken createNewPermanentToken(Card card, PermanentTokenRequest permanentTokenRequest) {
		String tokenValue = generateUniqueTokenValue();

		PermanentToken newToken = PermanentToken.builder()
			.cardUniqueNumber(card.getCardUniqueNumber().getValue())
			.token(tokenValue)
			.createdAt(LocalDateTime.now())
			.isActive(true)
			.card(card)
			.build();

		// 토큰 저장
		cardRepository.updateToken(newToken);
		// 카드 저장
		cardRepository.updateCard(card.updateCard(permanentTokenRequest));

		return newToken;
	}

	private String generateUniqueTokenValue() {
		return UUID.randomUUID().toString();
	}

	@Transactional(readOnly = true)
	public List<CardResponse> getCardsByUserApiKey(String userApiKey) {
		Optional<User> optionalUser = userRepository.findByUserApiKey(userApiKey);

		// Optional에서 User를 안전하게 꺼내서 사용
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			List<Card> cards = cardRepository.findByUserCIAndDeletedAtIsNull(user.getUserCI());

			// Card 객체들을 CardResponse로 변환
			return cards.stream()
				.map(card -> {
					CardResponse response = new CardResponse();
					response.setCardUniqueNumber(card.getCardUniqueNumber().getValue());
					response.setCardName(card.getCardName());
					return response;
				})
				.collect(Collectors.toList());
		} else {
			throw new UserNotFoundException("User not found with API key: " + userApiKey);
		}
	}
}