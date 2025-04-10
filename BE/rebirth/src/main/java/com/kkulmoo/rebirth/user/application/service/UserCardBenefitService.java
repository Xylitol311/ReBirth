package com.kkulmoo.rebirth.user.application.service;

import com.kkulmoo.rebirth.analysis.infrastructure.entity.ReportCardsEntity;
import com.kkulmoo.rebirth.analysis.infrastructure.repository.ReportCardsJpaRepository;
import com.kkulmoo.rebirth.card.domain.MyCard;
import com.kkulmoo.rebirth.transactions.application.dto.CardTransactionResponse;
import com.kkulmoo.rebirth.user.domain.UserCardBenefit;
import com.kkulmoo.rebirth.user.domain.repository.UserCardBenefitRepository;
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
        log.info("updateUserCardBenefit 실행, 거래내역 갯수: {}, 카드목록 갯수: {}", cardTransactionResponses.size(), cards.size());

        // 실적 조회 시 사용할 카드 (고유번호:보유카드 ID) Map
        Map<String, Integer> cardIdMap = new HashMap<>();
        for (MyCard myCard : cards) {
            cardIdMap.put(myCard.getCardUniqueNumber(), myCard.getCardId());
        }

        // 가져온 혜택 현황을 임시 저장할 객체 map (benefitId : UserCardBenefit)
        // 혜택 ID + 년도 + 월 정보를 조합하여 String 타입의 키로 사용
        Map<String, UserCardBenefit> userCardBenefitMap = new HashMap<>();

        for (CardTransactionResponse cardTransactionResponse : cardTransactionResponses) {
            int year = cardTransactionResponse.getCreatedAt().getYear();
            int month = cardTransactionResponse.getCreatedAt().getMonthValue();

            System.out.println(year + ": year");
            System.out.println(month + ": month");

            String key = cardTransactionResponse.getBenefitId() + "-" + year + "-" + month;

            // 이번 달 혜택 정보 가져오기
            // 1. map 먼저 확인
            UserCardBenefit temp = userCardBenefitMap.get(key);

            // 2. map에 없으면 db에서 조회(db에도 없으면 메서드 내부에서 새로 생성)
            if (temp == null) {
                temp = getUserCardBenefit(
                        cardTransactionResponse.getUserId().getValue()
                        , cardTransactionResponse.getBenefitId()
                        , cardIdMap.get(cardTransactionResponse.getCardUniqueNumber())
                        , cardTransactionResponse.getCreatedAt()
                );
            }

            // 거절 건이 아니면서 실적이 있는 경우에만 업데이트
            if (!cardTransactionResponse.getApprovalCode().startsWith("REJ") && cardTransactionResponse.getBenefitAmount() != 0) {
                log.info("승인 거래, 실적 확인: 혜택 현황 업데이트: {}", temp.toString());
                temp = temp.toBuilder()
                        .benefitCount((short) (temp.getBenefitCount() + 1))
                        .benefitAmount(temp.getBenefitAmount() + cardTransactionResponse.getBenefitAmount())
                        .build();
            }

            // 수정된 객체를 Map에 저장
            userCardBenefitMap.put(key, temp);
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

        // 실적 구간 조회
        Optional<ReportCardsEntity> reportCardsEntity = reportCardsJpaRepository.getByUserIdAndCardIdAndYearAndMonth(
                userId,
                cardId,
                lastYear,
                lastMonth);

        // 실적 구간 조회가 안되면 0으로 처리
        short spendingTier;
        if (reportCardsEntity.isPresent()) {
            spendingTier = reportCardsEntity.get().getSpendingTier();
        } else {
            spendingTier = 0;
        }

        return UserCardBenefit.builder()
                .userId(userId)
                .benefitTemplateId(benefitId)
                .spendingTier(spendingTier)
                .benefitCount((short) 0)
                .benefitAmount(0)
                .year(createdAt.getYear())
                .month(createdAt.getMonthValue())
                .build();
    }
}
