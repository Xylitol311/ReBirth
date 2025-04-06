package com.kkulmoo.rebirth.card.application;

import com.kkulmoo.rebirth.analysis.infrastructure.entity.ReportCardsEntity;
import com.kkulmoo.rebirth.analysis.infrastructure.repository.CategoryJpaRepository;
import com.kkulmoo.rebirth.analysis.infrastructure.repository.ReportCardsJpaRepository;
import com.kkulmoo.rebirth.card.application.dto.CardBenefit;
import com.kkulmoo.rebirth.card.application.dto.CardDetailResponse;
import com.kkulmoo.rebirth.card.application.dto.CardResponse;
import com.kkulmoo.rebirth.card.domain.*;
import com.kkulmoo.rebirth.card.infrastructure.adapter.dto.CardApiResponse;
import com.kkulmoo.rebirth.card.presentation.dto.CardOrderRequest;
import com.kkulmoo.rebirth.common.exception.CardProcessingException;
import com.kkulmoo.rebirth.payment.domain.UserCardBenefit;
import com.kkulmoo.rebirth.payment.infrastructure.repository.UserCardBenefitRepositoryImpl;
import com.kkulmoo.rebirth.shared.entity.CardTemplateEntity;
import com.kkulmoo.rebirth.user.domain.User;
import com.kkulmoo.rebirth.user.domain.UserId;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;
    private final CardPort cardPort;
    private final ReportCardsJpaRepository reportCardsJpaRepository;
    private final BenefitRepository benefitRepository;
    private final CategoryJpaRepository categoryJpaRepository;
    private final UserCardBenefitRepositoryImpl userCardBenefitRepository;

    @Transactional
    public CardDetailResponse getCardDetail(UserId userId, Integer cardId) {
        MyCard card = cardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID를 찾을 수 없습니다."));

        CardTemplateEntity cardTemplate = cardRepository.findCardTemplateEntityById(card.getCardTemplateId())
                .orElseThrow(() -> new EntityNotFoundException("해당 cardTemplate을 찾을 수 없습니다"));

        Integer maxPerformanceAmount = 0;
        try {
            maxPerformanceAmount = cardTemplate.getPerformanceRange().get(cardTemplate.getPerformanceRange().size() - 1);
        } catch (IndexOutOfBoundsException e) {
            // 인덱스가 범위를 벗어난 경우 0으로 설정
        }
        System.out.println("바보~~~~~~~~~~~");
        System.out.println(Arrays.toString(cardTemplate.getPerformanceRange().toArray()));
        ReportCardsEntity reportCardsEntity = reportCardsJpaRepository.getByUserIdAndCardIdAndYearAndMonth(
                        userId.getValue(),
                        cardId,
                        LocalDateTime.now().getYear(),
                        LocalDateTime.now().getMonthValue())
                .orElseThrow(() -> new EntityNotFoundException("해당 entity가 없습니다."));
        Short spendingTier = reportCardsEntity.getSpendingTier();
        if (spendingTier == null) {
            spendingTier = 0;  // 기본값 설정
        }


        List<BenefitTemplate> benefitTemplates = benefitRepository.findByTemplateId(cardTemplate.getCardTemplateId());

        int amountRemainingNext = maxPerformanceAmount - reportCardsEntity.getMonthSpendingAmount();
        if (amountRemainingNext < 0) amountRemainingNext = 0;


        List<CardBenefit> cardBenefits = new ArrayList<>();

        for (BenefitTemplate benefitTemplate : benefitTemplates) {
            List<Integer> categoryId = benefitTemplate.getCategoryId();
            List<String> categoryString = categoryJpaRepository.findByCategoryIdInOrderByCategoryId(categoryId);
            UserCardBenefit byUserIdAndBenefitId = userCardBenefitRepository.findByUserIdAndBenefitId(userId.getValue(), benefitTemplate.getBenefitId());

            cardBenefits.add(
                    CardBenefit.builder()
                            .benefitCategory(categoryString)
                            .receivedBenefitAmount(byUserIdAndBenefitId.getBenefitAmount())
                            .remainingBenefitAmount(
                                    calculateRemainingBenefit(
                                            benefitTemplate,
                                            spendingTier,
                                            byUserIdAndBenefitId.getBenefitAmount()
                                    )
                            )
                            .build());

        }


        return CardDetailResponse.builder()
                .cardId(cardId)
                .cardImageUrl(cardTemplate.getCardImgUrl())
                .cardName(cardTemplate.getCardName())
                .maxPerformanceAmount(maxPerformanceAmount)
                .currentPerformanceAmount(reportCardsEntity.getMonthSpendingAmount())
                .spendingMaxTier((short) cardTemplate.getPerformanceRange().size())
                .currentSpendingTier(spendingTier)  // 여기를 변경: reportCardsEntity.getSpendingTier() -> spendingTier
                .amountRemainingNext(amountRemainingNext)
                .performanceRange(cardTemplate.getPerformanceRange())
                .cardBenefits(cardBenefits)
                .build();

    }

    private Integer calculateRemainingBenefit(BenefitTemplate template, int spendingTier, Integer receivedAmount) {
        try {
            // 총 혜택 금액 가져오기 (null이면 0 사용)
            List<Short> amounts = template.getBenefitUsageAmount();
            Short totalAmount = (amounts != null && spendingTier < amounts.size() && amounts.get(spendingTier) != null)
                    ? amounts.get(spendingTier)
                    : 0;

            // receivedAmount가 null이면 0으로 처리
            int received = receivedAmount != null ? receivedAmount : 0;

            // 남은 혜택 금액 계산 (음수면 0 반환)
            int remaining = totalAmount - received;
            return  Math.max(0, remaining);
        } catch (Exception e) {
            return 0; // 예외 발생 시 0 반환
        }
    }

    @Transactional
    public void updateCardsOrder(UserId userId, List<CardOrderRequest> cardOrders) {
        // 요청된 카드 ID 수집
        List<Integer> cardIds = cardOrders.stream()
                .map(CardOrderRequest::getCardId)
                .collect(Collectors.toList());


        // 사용자의 카드만 조회 (보안을 위해 사용자 ID 확인)
        List<MyCard> userCards = cardRepository.findByUserIdAndCardIdIn(userId.getValue(), cardIds);

        // 카드 ID로 맵 생성
        Map<Integer, MyCard> cardMap = userCards.stream()
                .collect(Collectors.toMap(MyCard::getCardId, card -> card));

        // 각 카드의 새 위치 설정
        // map으로 해야하는 이유는 List로 한다면 n^2으로 찾아야한다.
        for (CardOrderRequest request : cardOrders) {
            MyCard card = cardMap.get(request.getCardId());
            if (card != null) {
                card.changeCardOrder(request.getPosition());
            }
        }

        // 변경된 카드 저장
        cardRepository.saveAll(cardMap.values());
    }

    public List<CardResponse> findCardsAll(UserId userId) {

        List<MyCard> userCards = findByUserId(userId);

        List<CardResponse> responses = new ArrayList<>();

        //카드의 최대 혜택양 계산하기!

        for (MyCard card : userCards) {
            CardTemplateEntity template = cardRepository.findCardTemplateEntityById(card.getCardTemplateId())
                    .orElseThrow(() -> new EntityNotFoundException("카드 템플릿을 찾을 수 없습니다: " + card.getCardTemplateId()));

            ReportCardsEntity latestReport = reportCardsJpaRepository.getByUserIdAndCardIdAndYearAndMonth(
                            userId.getValue(),
                            card.getCardId(),
                            LocalDate.now().getYear(),
                            LocalDate.now().getMonthValue())
                    .orElseThrow(() -> new EntityNotFoundException("reportEntity를 찾을 수 없습니다. "));


            List<Integer> performanceRange = template.getPerformanceRange();

            //이걸 가지고 혜택을 불러와서 혜택을 모두 불러와!! 혜택은 뭘로할 수 있을까?

            Short lastMonthSpendingTier = card.getSpendingTier();
            List<BenefitTemplate> benefitTemplates = benefitRepository.findByTemplateId(card.getCardTemplateId());

            Integer maxBenefitAmount = benefitTemplates.stream()
                    .filter(Objects::nonNull)
                    .mapToInt(benefit -> {
                        try {
                            Short value = benefit.getBenefitUsageAmount().get(lastMonthSpendingTier);
                            return value != null ? value : 0;
                        } catch (Exception e) {
                            return 0; // 예외 발생 시 0 반환
                        }
                    })
                    .sum();

            //일단 나의 실적 구간을 알아야 한다.
            // 총 소비
            Integer totalSpending = (latestReport != null) ? latestReport.getMonthSpendingAmount() : 0;
            // 최대 실적 구간
            Integer maxSpending = performanceRange.isEmpty() ? null : performanceRange.get(performanceRange.size() - 1);
            // 받은 혜택량
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
    public List<MyCard> findByUserId(UserId userId) {
        return cardRepository.findByUserId(userId);
    }

    public List<MyCard> getMyCardListByCardUniqueNumbers(List<String> cardUniqueNumbers) {
        return cardRepository.findByCardUniqueNumbers(cardUniqueNumbers);
    }

    // 모든 카드데이터 불러오기.
    // 컨트롤러에서 호출할 수 있는 메서드
    public List<MyCard> getCardData(User user) {
        try {
            log.info("사용자 {}의 카드 데이터 처리 시작", user.getUserName());

            // 1. 현재 DB에 있는 사용자의 카드 조회
            List<MyCard> existingMyCards = cardRepository.findByUserId(user.getUserId());
            log.info("사용자 {}의 기존 카드 {}개가 DB에 존재합니다", user.getUserName(), existingMyCards.size());

            // 기존 카드의 고유 번호를 Set으로 변환 (검색 최적화)
            Set<String> existingCardNumbers = existingMyCards.stream()
                    .map(MyCard::getCardUniqueNumber)
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
    private List<MyCard> processNewCards(List<CardApiResponse> apiCards, Set<String> existingCardNumbers, User user) {
        log.info("사용자 {}의 새 카드 처리 시작", user.getUserName());
        List<MyCard> newCards = new ArrayList<>();  // 새로 생성된 카드들을 저장할 리스트

        for (CardApiResponse apiCard : apiCards) {
            String cardUniqueNumber = apiCard.getCardUniqueNumber();

            // 이미 DB에 있는 카드는 건너뜀
            if (existingCardNumbers.contains(cardUniqueNumber)) {
                log.debug("카드 {}는 이미 DB에 존재합니다", cardUniqueNumber);
                continue;
            }

            // 새 카드 생성
            MyCard myCard = createCard(apiCard, user.getUserId());
            log.info("새 카드가 생성되었습니다: {}", cardUniqueNumber);
            newCards.add(myCard);  // 생성된 카드를 리스트에 추가
        }
        return newCards;  // 생성된 모든 카드 반환

    }


    // 카드 존재 여부 확인 메서드
    private MyCard createCard(CardApiResponse apiCard, UserId userId) {

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
        Short newCardOrder = (short) (existingCardCount + 1);

        LocalDateTime defaultDate = LocalDateTime.of(2000, 1, 1, 0, 0, 0);


        // 새 카드 객체 생성
        MyCard newMyCard = MyCard.builder()
                .userId(userId)
                .cardTemplateId(cardTemplate.getCardTemplateId())
                .cardUniqueNumber(apiCard.getCardUniqueNumber())
                .annualFee(cardTemplate.getAnnualFee())
                .cardName(cardTemplate.getCardName())
                .cardOrder(newCardOrder)
                .latestLoadDataAt(defaultDate)
                .build();

        // 저장 후 반환
        return cardRepository.save(newMyCard);
    }


    public void updateCardsLastLoadTime(List<MyCard> myCard) {
        List<MyCard> updatedCards = myCard.stream()
                .map(MyCard::updateLatestLoadDataAt)
                .collect(Collectors.toList());

        cardRepository.saveAll(updatedCards);
    }

}
