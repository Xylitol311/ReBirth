package com.kkulmoo.rebirth.card.application;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.kkulmoo.rebirth.card.domain.Card;
import com.kkulmoo.rebirth.card.domain.CardRepository;
import com.kkulmoo.rebirth.payment.domain.CardTemplate;
import com.kkulmoo.rebirth.user.domain.User;
import com.kkulmoo.rebirth.user.domain.UserId;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CardService {
	private final ApplicationEventPublisher eventPublisher;
	private final CardRepository cardRepository;
	private final CardPort cardPort; // 인터페이스 의존

	// 처음에 Card정보를 가져와서 create하자..!
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void getCardFromMyDataListener(MyDataEvent myDataEvent) {
		User user = myDataEvent.getUser();
		getCardData(user);
	}

	// 컨트롤러에서 호출할 수 있는 메서드
	public void getCardData(User user) {
		try {
			log.info("사용자 {}의 카드 데이터 처리 시작", user.getUserName());

			// 1. 현재 DB에 있는 사용자의 카드 조회
			List<Card> existingCards = cardRepository.findByUserId(user.getUserId());
			log.info("사용자 {}의 기존 카드 {}개가 DB에 존재합니다", user.getUserName(), existingCards.size());

			// 기존 카드의 고유 번호를 Set으로 변환 (검색 최적화)
			Set<String> existingCardNumbers = existingCards.stream()
				.map(Card::getCardUniqueNumber)
				.collect(Collectors.toSet());

			// 2. API를 통해 최신 카드 정보 가져오기
			List<CardApiResponse> apiCards = cardPort.fetchCardData(user);
			log.info("API에서 사용자 {}의 카드 {}개를 가져왔습니다", user.getUserName(), apiCards.size());

			// 3. 아직 DB에 없는 카드만 처리
			processNewCards(apiCards, existingCardNumbers, user);

		} catch (Exception e) {
			log.error("카드 데이터 처리 중 오류 발생 (사용자: {}): {}", user.getUserName(), e.getMessage());
			throw new CardProcessingException("카드 데이터 처리에 실패했습니다", e);
		}
	}


	/**
	 * API에서 가져온 카드 중 DB에 없는 것만 처리하는 메서드
	 */
	private void processNewCards(List<CardApiResponse> apiCards, Set<String> existingCardNumbers, User user) {
		log.info("사용자 {}의 새 카드 처리 시작", user.getUserName());
		int newCardCount = 0;

		for (CardApiResponse apiCard : apiCards) {
			String cardUniqueNumber = apiCard.getCardUniqueNumber();

			// 이미 DB에 있는 카드는 건너뜀
			if (existingCardNumbers.contains(cardUniqueNumber)) {
				log.debug("카드 {}는 이미 DB에 존재합니다", cardUniqueNumber);
				continue;
			}

			// 새 카드 생성
			createCard(apiCard, user.getUserId());
			log.info("새 카드가 생성되었습니다: {}", cardUniqueNumber);
			newCardCount++;
		}

		log.info("사용자 {}의 카드 처리 완료. {}개의 새 카드가 생성됨", user.getUserName(), newCardCount);
	}


	// 카드 존재 여부 확인 메서드
	private Card createCard(CardApiResponse apiCard, UserId userId) {
		// 카드 템플릿 가져오기
		Optional<CardTemplate> cardTemplateOptional = cardRepository.findCardTemplateByCardName(apiCard.getCardName());

		if (cardTemplateOptional.isEmpty()) {
			log.error("카드 '{}' 템플릿을 찾을 수 없습니다", apiCard.getCardName());
			throw new CardProcessingException("카드 템플릿을 찾을 수 없습니다: " + apiCard.getCardName());
		}

		CardTemplate cardTemplate = cardTemplateOptional.get();

		// 새 카드 객체 생성
		Card newCard = Card.builder()
			.userId(userId)
			.cardTemplateId(cardTemplate.getCardTemplateId())
			.cardUniqueNumber(apiCard.getCardUniqueNumber())
			.build();

		// 저장 후 반환
		return cardRepository.save(newCard);
	}
}
