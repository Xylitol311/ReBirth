package com.kkulmoo.rebirth.user.application.service;

import com.kkulmoo.rebirth.analysis.infrastructure.entity.ReportCardsEntity;
import com.kkulmoo.rebirth.analysis.infrastructure.repository.ReportCardsJpaRepository;
import com.kkulmoo.rebirth.card.domain.MyCard;
import com.kkulmoo.rebirth.transactions.application.dto.CardTransactionResponse;
import com.kkulmoo.rebirth.user.domain.UserCardBenefit;
import com.kkulmoo.rebirth.user.domain.repository.UserCardBenefitRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCardBenefitService {
    private final UserCardBenefitRepository userCardBenefitRepository;
    private final ReportCardsJpaRepository reportCardsJpaRepository;

    public void updateUseCardBenefit(List<CardTransactionResponse> cardTransactionResponses, List<MyCard> cards) {
        log.info("임시저장소 유저 혜택 저장소 ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");

        // 실적 조회 시 사용할 카드 (고유번호:보유카드 ID) Map
        Map<String, Integer> cardIdMap = new HashMap<>();
        for (MyCard myCard : cards) {
            cardIdMap.put(myCard.getCardUniqueNumber(), myCard.getCardId());
        }

        // 가져온 혜택 현황을 임시 저장할 객체 map (benefitId : UserCardBenefit)
        Map<Integer, UserCardBenefit> userCardBenefitMap = new HashMap<>();

        for (CardTransactionResponse cardTransactionResponse : cardTransactionResponses) {
            int year = cardTransactionResponse.getCreatedAt().getYear();
            int month = cardTransactionResponse.getCreatedAt().getMonthValue();

            System.out.println(year + ": year");
            System.out.println(month + ": month");

            // 이번 달 혜택 정보 가져오기
            UserCardBenefit temp = getUserCardBenefit(
                    cardTransactionResponse.getUserId().getValue()
                    , cardTransactionResponse.getBenefitId()
                    , cardIdMap.get(cardTransactionResponse.getCardUniqueNumber())
                    , cardTransactionResponse.getCreatedAt()
            );

            // 거절 건이 아니면서 실적이 있는 경우에만 업데이트
            log.info("거절 건이 아니면서 실적이 있는 경우에만 업데이트: {}", temp.toString());
            if (!cardTransactionResponse.getApprovalCode().startsWith("REJ") && cardTransactionResponse.getBenefitAmount() != 0) {
                temp = temp.toBuilder()
                        .benefitCount((short) (temp.getBenefitCount() + 1))
                        .benefitAmount(temp.getBenefitAmount() + cardTransactionResponse.getBenefitAmount())
                        .build();
            }

            // 수정된 객체를 Map에 저장
            userCardBenefitMap.put(cardTransactionResponse.getBenefitId(), temp);
        }
        // userCardBenefitMap에 있는 객체들 updatedAt 바꿔서 UserCardBenefit db에 저장.
        List<UserCardBenefit> list = userCardBenefitMap.values()
                .stream()
                .map(benefit -> benefit.toBuilder()
                        .updateDate(LocalDateTime.now())
                        .build())
                .collect(Collectors.toList());
        userCardBenefitRepository.saveAll(list);
    }

    public short findSpendingTier(CardTransactionResponse cardTransactionResponse, Integer cardId) {
        int month = cardTransactionResponse.getCreatedAt().getMonthValue() == 1 ?
                12 : cardTransactionResponse.getCreatedAt().getMonthValue() - 1;
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

    public UserCardBenefit getUserCardBenefit(int userId, Integer benefitId, Integer cardId, LocalDateTime createdAt) {
        Optional<UserCardBenefit> userCardBenefitOptional = userCardBenefitRepository.findByUserIdAndBenefitTemplateIdAndYearAndMonth(
                userId,
                benefitId,
                createdAt.getYear(),
                createdAt.getMonthValue()
        );

        if (userCardBenefitOptional.isPresent()) {
            return userCardBenefitOptional.get();
        }


        int lastMonth = createdAt.getMonthValue() == 1 ?
                12 : createdAt.getMonthValue();
        int lastYear = createdAt.getMonthValue() == 1 ?
                createdAt.getYear() - 1
                : createdAt.getYear();
        short spendingTier = reportCardsJpaRepository.getByUserIdAndCardIdAndYearAndMonth(
                userId,
                cardId,
                lastYear,
                lastMonth).get().getSpendingTier();

        UserCardBenefit userCardBenefit = UserCardBenefit.builder()
                .userId(userId)
                .benefitTemplateId(benefitId)
                .spendingTier(spendingTier)
                .benefitCount((short) 0)
                .benefitAmount(0)
                .year(createdAt.getYear())
                .month(createdAt.getMonthValue())
                .build();

        return userCardBenefit;
    }

}
