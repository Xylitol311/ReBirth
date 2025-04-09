package com.kkulmoo.rebirth.user.application.service;

import com.kkulmoo.rebirth.analysis.infrastructure.entity.ReportCardsEntity;
import com.kkulmoo.rebirth.analysis.infrastructure.repository.ReportCardsJpaRepository;
import com.kkulmoo.rebirth.card.domain.MyCard;
import com.kkulmoo.rebirth.transactions.application.dto.CardTransactionResponse;
import com.kkulmoo.rebirth.user.domain.UserCardBenefit;
import com.kkulmoo.rebirth.user.domain.repository.UserCardBenefitRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserCardBenefitService {
    private final UserCardBenefitRepository userCardBenefitRepository;
    private final ReportCardsJpaRepository reportCardsJpaRepository;

    public void updateUseCardBenefit(List<CardTransactionResponse> cardTransactionResponses, List<MyCard> cards) {
        // 실적 조회 시 사용할 카드 (고유번호:보유카드 ID) Map
        Map<String, Integer> cardIdMap = new HashMap<>();
        for(MyCard myCard : cards){
            cardIdMap.put(myCard.getCardUniqueNumber(), myCard.getCardId());
        }

        // 가져온 혜택 현황을 임시 저장할 객체 map (benefitId : UserCardBenefit)
        Map<Integer, UserCardBenefit> userCardBenefitMap = new HashMap<>();

        for (CardTransactionResponse cardTransactionResponse : cardTransactionResponses) {
            int year = cardTransactionResponse.getCreatedAt().getYear();
            int month = cardTransactionResponse.getCreatedAt().getMonthValue();

            UserCardBenefit temp = null;
            // 이번 달 혜택 정보 가져오기
            // 1. Map에 있는지 먼저 확인
            if (userCardBenefitMap.containsKey(cardTransactionResponse.getBenefitId())){
                temp = userCardBenefitMap.get(cardTransactionResponse.getBenefitId());
            }

            // 2. Map에 없으면 DB에서 조회
            else {
                temp = userCardBenefitRepository.findByUserIdAndBenefitTemplateIdAndYearAndMonth(
                        cardTransactionResponse.getUserId().getValue(),
                        cardTransactionResponse.getBenefitId(),
                        year,
                        month
                ).orElseGet(() -> UserCardBenefit.builder() // DB에 데이터가 없으면 새로 생성
                        .userId(cardTransactionResponse.getUserId().getValue())
                        .userCardBenefitId(cardTransactionResponse.getBenefitId())
                        .benefitTemplateId(cardTransactionResponse.getBenefitId())
                        .spendingTier( // 리포트에서 전월 실적 구간 가져오기
                                findSpendingTier(
                                        cardTransactionResponse
                                        , cardIdMap.get(cardTransactionResponse.getCardUniqueNumber()
                                        )
                                )
                        )
                        .benefitCount((short)0)
                        .benefitAmount(0)
                        .year(year)
                        .month(month)
                        .build());
            }

            // 거절 건이 아니면서 실적이 있는 경우에만 업데이트
            if (!cardTransactionResponse.getApprovalCode().startsWith("REG") && cardTransactionResponse.getBenefitAmount() != 0) {
                temp = temp.toBuilder()
                        .benefitCount((short)(temp.getBenefitCount() + 1))
                        .benefitAmount(temp.getBenefitAmount() + cardTransactionResponse.getBenefitAmount())
                        .build();

            }

            // 수정된 객체를 Map에 저장
            userCardBenefitMap.put(cardTransactionResponse.getBenefitId(), temp);
        }
        // userCardBenefitMap에 있는 객체들 updatedAt 바꿔서 UserCardBenefit db에 저장.
        LocalDateTime now = LocalDateTime.now();
        List<UserCardBenefit> list = userCardBenefitMap.values()
                .stream()
                .map(benefit -> benefit.toBuilder()
                        .updateDate(now)
                        .build())
                .collect(Collectors.toList());
        userCardBenefitRepository.saveAll(list);
    }

    private short findSpendingTier(CardTransactionResponse cardTransactionResponse, Integer cardId) {
        int month = cardTransactionResponse.getCreatedAt().getMonthValue() == 1 ?
                12 : cardTransactionResponse.getCreatedAt().getMonthValue();
        int year = cardTransactionResponse.getCreatedAt().getMonthValue() == 1 ?
                cardTransactionResponse.getCreatedAt().getYear() - 1
                : cardTransactionResponse.getCreatedAt().getYear();

        ReportCardsEntity latestReport = reportCardsJpaRepository.getByUserIdAndCardIdAndYearAndMonth(
                        cardTransactionResponse.getUserId().getValue(),
                        cardId,
                        year,
                        month)
                .orElseThrow(() -> new EntityNotFoundException("reportEntity를 찾을 수 없습니다. "));
        return latestReport.getSpendingTier();
    }


}
