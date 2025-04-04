package com.kkulmoo.rebirth.card.application;

import com.kkulmoo.rebirth.analysis.infrastructure.entity.ReportCardsEntity;
import com.kkulmoo.rebirth.analysis.infrastructure.repository.ReportCardsJpaRepository;
import com.kkulmoo.rebirth.card.application.dto.CardResponse;
import com.kkulmoo.rebirth.card.domain.CardRepository;
import com.kkulmoo.rebirth.card.domain.CardTemplate;
import com.kkulmoo.rebirth.card.domain.myCard;
import com.kkulmoo.rebirth.card.infrastructure.adapter.dto.CardApiResponse;
import com.kkulmoo.rebirth.card.presentation.dto.CardOrderRequest;
import com.kkulmoo.rebirth.common.exception.CardProcessingException;
import com.kkulmoo.rebirth.shared.entity.CardTemplateEntity;
import com.kkulmoo.rebirth.user.domain.User;
import com.kkulmoo.rebirth.user.domain.UserId;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;
    private final CardPort cardPort; // 인터페이스 의존
    private final ReportCardsJpaRepository reportCardsJpaRepository;

    @Transactional
    public void updateCardsOrder(UserId userId, List<CardOrderRequest> cardOrders) {
        // 요청된 카드 ID 수집
        List<Integer> cardIds = cardOrders.stream()
                .map(CardOrderRequest::getCardId)
                .collect(Collectors.toList());

        // 사용자의 카드만 조회 (보안을 위해 사용자 ID 확인)
        List<myCard> userCards = cardRepository.findByUserIdAndCardIdIn(userId.getValue(), cardIds);

        // 카드 ID로 맵 생성
        Map<Integer, myCard> cardMap = userCards.stream()
                .collect(Collectors.toMap(myCard::getCardId, card -> card));

        // 각 카드의 새 위치 설정
        // map으로 해야하는 이유는 List로 한다면 n^2으로 찾아야한다.
        for (CardOrderRequest request : cardOrders) {
            myCard card = cardMap.get(request.getCardId());
            if (card != null) {
                card.changeCardOrder(request.getPosition());
            }
        }

        // 변경된 카드 저장
        cardRepository.saveAll(cardMap.values());
    }

    public List<CardResponse> findCardsAndBenefitByUserId(UserId userId) {

        List<myCard> userCards = findByUserId(userId);

        List<CardResponse> responses = new ArrayList<>();

        for (myCard card : userCards) {
            CardTemplateEntity template = cardRepository.findCardTemplateEntityById(card.getCardTemplateId())
                    .orElseThrow(() -> new EntityNotFoundException("카드 템플릿을 찾을 수 없습니다: " + card.getCardTemplateId()));

            ReportCardsEntity latestReport = reportCardsJpaRepository
                    .findTopByCardIdOrderByCreatedAtDesc(card.getCardId())
                    .orElse(null);

            List<Integer> performanceRange = template.getPerformanceRange();
            Integer maxSpending = performanceRange.isEmpty() ? null : performanceRange.get(performanceRange.size() - 1);

            Integer maxBenefitAmount = 0; // 아직 계산 못함.

            Integer totalSpending = (latestReport != null) ? latestReport.getMonthSpendingAmount() : 0;
            Integer receivedBenefitAmount = (latestReport != null) ? latestReport.getMonthBenefitAmount() : 0;


            responses.add(CardResponse.builder()
                    .cardId(card.getCardId())
                    .cardImgUrl(template.getCardImgUrl())
                    .cardName(template.getCardName())
                    .totalSpending(totalSpending)
                    .maxSpending(maxSpending)
                    .performanceRange(performanceRange)
                    .receivedBenefitAmount(receivedBenefitAmount)
                    .maxBenefitAmount(maxBenefitAmount)
                    .build());

        }
        return responses;
    }

    // 카드 정보만 가져오는 경우
    public List<myCard> findByUserId(UserId userId) {
        return cardRepository.findByUserId(userId);
    }

    public List<myCard> getMyCardListByCardUniqueNumbers(List<String> cardUniqueNumbers) {
        return cardRepository.findByCardUniqueNumbers(cardUniqueNumbers);
    }

    // 모든 카드데이터 불러오기.
    // 컨트롤러에서 호출할 수 있는 메서드
    public List<myCard> getCardData(User user) {
        try {
            log.info("사용자 {}의 카드 데이터 처리 시작", user.getUserName());

            // 1. 현재 DB에 있는 사용자의 카드 조회
            List<myCard> existingMyCards = cardRepository.findByUserId(user.getUserId());
            log.info("사용자 {}의 기존 카드 {}개가 DB에 존재합니다", user.getUserName(), existingMyCards.size());

            // 기존 카드의 고유 번호를 Set으로 변환 (검색 최적화)
            Set<String> existingCardNumbers = existingMyCards.stream()
                    .map(myCard::getCardUniqueNumber)
                    .collect(Collectors.toSet());

            // 2. API를 통해 최신 카드 정보 가져오기
            List<CardApiResponse> apiCards = cardPort.fetchCardData(user);
            log.info("API에서 사용자 {}의 카드 {}개를 가져왔습니다", user.getUserName(), apiCards.size());

            // 3. 아직 DB에 없는 카드만 처리
            return processNewCards(apiCards, existingCardNumbers, user);

        } catch (Exception e) {
            log.error("카드 데이터 처리 중 오류 발생 (사용자: {}): {}", user.getUserName(), e.getMessage());
            throw new CardProcessingException("카드 데이터 처리에 실패했습니다", e);
        }
    }

    /**
     * API에서 가져온 카드 중 DB에 없는 것만 처리하는 메서드
     */
    private List<myCard> processNewCards(List<CardApiResponse> apiCards, Set<String> existingCardNumbers, User user) {
        log.info("사용자 {}의 새 카드 처리 시작", user.getUserName());
        List<myCard> newCards = new ArrayList<>();  // 새로 생성된 카드들을 저장할 리스트

        for (CardApiResponse apiCard : apiCards) {
            String cardUniqueNumber = apiCard.getCardUniqueNumber();

            // 이미 DB에 있는 카드는 건너뜀
            if (existingCardNumbers.contains(cardUniqueNumber)) {
                log.debug("카드 {}는 이미 DB에 존재합니다", cardUniqueNumber);
                continue;
            }

            // 새 카드 생성
            myCard myCard = createCard(apiCard, user.getUserId());
            log.info("새 카드가 생성되었습니다: {}", cardUniqueNumber);
            newCards.add(myCard);  // 생성된 카드를 리스트에 추가
        }
        return newCards;  // 생성된 모든 카드 반환

    }


    // 카드 존재 여부 확인 메서드
    private myCard createCard(CardApiResponse apiCard, UserId userId) {

        System.out.println("이름 체크 하는 과정입니다이름 체크 하는 과정입니다이름 체크 하는 과정입니다");
        System.out.println(apiCard.getCardName());
        // 카드 템플릿 가져오기
        Optional<CardTemplate> cardTemplateOptional = cardRepository.findCardTemplateByCardName(apiCard.getCardName());

        if (cardTemplateOptional.isEmpty()) {
            log.error("카드 '{}' 템플릿을 찾을 수 없습니다", apiCard.getCardName());
            throw new CardProcessingException("카드 템플릿을 찾을 수 없습니다: " + apiCard.getCardName());
        }

        CardTemplate cardTemplate = cardTemplateOptional.get();

        Integer existingCardCount = cardRepository.countByUserId(userId);
        Short newCardOrder = (short)(existingCardCount + 1);



        // 새 카드 객체 생성
        myCard newMyCard = myCard.builder()
                .userId(userId)
                .cardTemplateId(cardTemplate.getCardTemplateId())
                .cardUniqueNumber(apiCard.getCardUniqueNumber())
                .annualFee(cardTemplate.getAnnualFee())
                .cardName(cardTemplate.getCardName())
                .cardOrder(newCardOrder)
                .build();

        // 저장 후 반환
        return cardRepository.save(newMyCard);
    }


    public void updateCardsLastLoadTime(List<myCard> myCards) {
        List<myCard> updatedCards = myCards.stream()
                .map(myCard::updateLatestLoadDataAt)
                .collect(Collectors.toList());

        cardRepository.saveAll(updatedCards);
    }

}
