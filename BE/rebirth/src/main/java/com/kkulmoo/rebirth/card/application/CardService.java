package com.kkulmoo.rebirth.card.application;

import com.kkulmoo.rebirth.card.domain.CardRepository;
import com.kkulmoo.rebirth.card.domain.CardTemplate;
import com.kkulmoo.rebirth.card.domain.myCard;
import com.kkulmoo.rebirth.card.infrastructure.adapter.dto.CardApiResponse;
import com.kkulmoo.rebirth.common.exception.CardProcessingException;
import com.kkulmoo.rebirth.user.domain.User;
import com.kkulmoo.rebirth.user.domain.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;
    private final CardPort cardPort; // 인터페이스 의존


    public List<myCard> getMyCardListByCardUniqueNumbers(List<String> cardUniqueNumbers) {
        return cardRepository.findByCardUniqueNumbers(cardUniqueNumbers);
    }

    public List<myCard> findByUserId(UserId userId){
        return cardRepository.findByUserId(userId);
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
        // 카드 템플릿 가져오기
        Optional<CardTemplate> cardTemplateOptional = cardRepository.findCardTemplateByCardName(apiCard.getCardName());

        if (cardTemplateOptional.isEmpty()) {
            log.error("카드 '{}' 템플릿을 찾을 수 없습니다", apiCard.getCardName());
            throw new CardProcessingException("카드 템플릿을 찾을 수 없습니다: " + apiCard.getCardName());
        }

        CardTemplate cardTemplate = cardTemplateOptional.get();

        // 새 카드 객체 생성
        myCard newMyCard = myCard.builder()
                .userId(userId)
                .cardTemplateId(cardTemplate.getCardTemplateId())
                .cardUniqueNumber(apiCard.getCardUniqueNumber())
                .build();

        // 저장 후 반환
        return cardRepository.save(newMyCard);
    }

}
