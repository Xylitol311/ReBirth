package com.kkulmoo.rebirth.payment.application.service;

import com.kkulmoo.rebirth.card.domain.BenefitRepository;
import com.kkulmoo.rebirth.card.domain.CardRepository;
import com.kkulmoo.rebirth.card.domain.DiscountType;
import com.kkulmoo.rebirth.card.domain.MyCard;
import com.kkulmoo.rebirth.payment.application.BenefitInfo;
import com.kkulmoo.rebirth.payment.domain.UserCardBenefit;
import com.kkulmoo.rebirth.payment.domain.repository.UserCardBenefitRepository;
import com.kkulmoo.rebirth.payment.infrastructure.dto.MerchantJoinDto;
import com.kkulmoo.rebirth.payment.presentation.response.CalculatedBenefitDto;
import com.kkulmoo.rebirth.user.domain.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

@Service
@RequiredArgsConstructor
@Slf4j
public class BenefitService {

    // 사용자 카드 정보를 조회하는 Repository
    private final CardRepository cardRepository;
    // 혜택 정보를 조회하는 Repository
    private final BenefitRepository benefitRepository;
    // 사용자 카드의 혜택 사용 정보를 조회하는 Repository
    private final UserCardBenefitRepository userCardBenefitRepository;

    // 추천 카드 혜택 계산 (모든 보유 카드에 대해 계산 후 최대 혜택 선택)
    public CalculatedBenefitDto recommendPaymentCard(Integer userId, int amount, MerchantJoinDto merchantJoinDto) {
        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();

        // 사용자 보유 카드 목록 조회
        UserId userId1 = new UserId(userId);
        List<MyCard> myCards = cardRepository.findByUserId(userId1);
        // 최대 혜택을 찾기 위한 우선순위 큐 생성 (내림차순)
        Queue<CalculatedBenefitDto> benefitQueue = new PriorityQueue<>(Comparator.comparingInt(CalculatedBenefitDto::getBenefitAmount).reversed());
        // 각 카드별 혜택 계산 수행
        for (MyCard card : myCards) {
            if (card.getPermanentToken() == null) continue; // 유효하지 않은 카드 건너뜀
            // 해당 카드에 적용 가능한 혜택 정보 조회
            List<BenefitInfo> benefitInfos = benefitRepository.findBenefitsByMerchantFilter(
                    card.getCardTemplateId(),
                    merchantJoinDto.getCategoryId(),
                    merchantJoinDto.getSubCategoryId(),
                    merchantJoinDto.getMerchantId()
            );
            // 각 혜택에 대해 할인 금액 계산
            for (BenefitInfo benefitInfo : benefitInfos) {
                if ("쿠폰".equals(benefitInfo.getBenefitType().toString())) continue; // 쿠폰은 제외
                UserCardBenefit userCardBenefit = userCardBenefitRepository.findByUserIdAndBenefitTemplateIdAndYearAndMonth(
                        userId,
                        benefitInfo.getBenefitId(),
                        currentYear,
                        currentMonth
                );
                int discountAmount = calculateBenefitAmount(benefitInfo, amount, userCardBenefit);
                CalculatedBenefitDto calculated = CalculatedBenefitDto.builder()
                        .myCardId(card.getCardId())
                        .permanentToken(card.getPermanentToken())
                        .benefitId(benefitInfo.getBenefitId())
                        .benefitAmount(discountAmount)
                        .benefitType(benefitInfo.getBenefitType())
                        .build();
                benefitQueue.add(calculated);
            }
        }
        if (!benefitQueue.isEmpty()) {
            return benefitQueue.poll(); // 최대 혜택 카드 반환
        }
        log.error("혜택 계산 실패. 로직이 끝났으나 결과값 없음");
        return null;
    }

    // 실제 카드의 혜택 계산 (제공된 영구토큰을 기반으로 단일 카드에 대해 계산)
    public CalculatedBenefitDto calculateRealBenefit(int userId, int amount, MerchantJoinDto merchantJoinDto, MyCard myCard) {
        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();

        // 해당 카드에 적용 가능한 혜택 정보 조회
        List<BenefitInfo> benefitInfos = benefitRepository.findBenefitsByMerchantFilter(
                myCard.getCardTemplateId(),
                merchantJoinDto.getCategoryId(),
                merchantJoinDto.getSubCategoryId(),
                merchantJoinDto.getMerchantId()
        );
        // 최대 혜택 계산을 위한 우선순위 큐 생성
        Queue<CalculatedBenefitDto> benefitQueue = new PriorityQueue<>(Comparator.comparingInt(CalculatedBenefitDto::getBenefitAmount).reversed());
        // 각 혜택에 대해 할인 금액 계산 수행
        for (BenefitInfo benefitInfo : benefitInfos) {
            if ("쿠폰".equals(benefitInfo.getBenefitType().toString())) continue; // 쿠폰은 제외
            UserCardBenefit userCardBenefit = userCardBenefitRepository.findByUserIdAndBenefitTemplateIdAndYearAndMonth(
                    userId,
                    benefitInfo.getBenefitId(),
                    currentYear,
                    currentMonth
            );
            int discountAmount = calculateBenefitAmount(benefitInfo, amount, userCardBenefit);
            CalculatedBenefitDto calculated = CalculatedBenefitDto.builder()
                    .myCardId(myCard.getCardId())
                    .permanentToken(myCard.getPermanentToken())
                    .benefitId(benefitInfo.getBenefitId())
                    .benefitAmount(discountAmount)
                    .benefitType(benefitInfo.getBenefitType())
                    .build();
            benefitQueue.add(calculated);
        }
        if (!benefitQueue.isEmpty()) {
            return benefitQueue.poll(); // 최대 혜택 계산 결과 반환
        }
        log.error("실제 카드 혜택 계산 실패. 결과값이 없음");
        return null;
    }

    // 혜택 금액 계산 메서드 (사용자 카드 혜택 정보를 기반으로 할인 금액 산출)
    public int calculateBenefitAmount(BenefitInfo benefitInfo, int amount, UserCardBenefit userCardBenefit) {
        // 최소 실적 미달인 경우 할인 없음
        if (userCardBenefit.getSpendingTier() == 0) return 0;
        double benefit = 0.0;
        int spendingTier = userCardBenefit.getSpendingTier();
        // 단일 또는 구간 조건일 경우
        if (benefitInfo.getBenefitConditionType() == 4 || benefitInfo.getBenefitConditionType() == 1) {
            if (benefitInfo.getBenefitsBySection() != null && benefitInfo.getBenefitsBySection().size() >= spendingTier) {
                benefit = benefitInfo.getBenefitsBySection().get(spendingTier - 1);
            }
        }
        // 건당 결제 금액 조건일 경우
        if (benefitInfo.getBenefitConditionType() == 2) {
            benefit = calculateBenefit(benefitInfo, amount);
        }
        // 복합 조건일 경우
        if (benefitInfo.getBenefitConditionType() == 3 && spendingTier >= 1) {
            benefit = calculateBenefit(benefitInfo, amount);
        }
        if (benefit == 0) return 0; // 할인 혜택이 0이면 바로 반환
        // 사용 제한(횟수) 체크
        if (benefitInfo.getBenefitUsageLimit() != null && benefitInfo.getBenefitUsageLimit().size() >= spendingTier) {
            if (userCardBenefit.getBenefitCount() >= benefitInfo.getBenefitUsageLimit().get(spendingTier - 1))
                return 0;
        }
        // 사용 제한(금액) 체크
        int totalAbleBenefitAmount = Integer.MAX_VALUE;
        if (benefitInfo.getBenefitUsageAmount() != null && benefitInfo.getBenefitUsageAmount().size() >= spendingTier) {
            totalAbleBenefitAmount = benefitInfo.getBenefitUsageAmount().get(spendingTier - 1);
        }
        if (userCardBenefit.getBenefitAmount() >= totalAbleBenefitAmount) return 0;
        int result;
        // 할인 타입에 따라 최종 할인 금액 산출
        if (benefitInfo.getDiscountType() == DiscountType.AMOUNT) {
            result = Math.min((int) benefit, totalAbleBenefitAmount - userCardBenefit.getBenefitAmount());
        } else {
            result = Math.min((int) (amount * benefit), totalAbleBenefitAmount - userCardBenefit.getBenefitAmount());
        }
        return result;
    }

    // 결제 구간 기준 할인 금액 계산 (세부 조건 적용)
    private double calculateBenefit(BenefitInfo benefitInfo, int amount) {
        double benefit = 0.0;
        int rangeIdx = 0;
        if (benefitInfo.getPaymentRange() != null && benefitInfo.getBenefitsBySection() != null) {
            for (int idx = 0; idx < benefitInfo.getPaymentRange().size(); idx++) {
                if (benefitInfo.getPaymentRange().get(idx) < amount) {
                    rangeIdx = idx + 1;
                    break;
                }
            }
            if (rangeIdx != 0 && benefitInfo.getBenefitsBySection().size() >= rangeIdx) {
                benefit = benefitInfo.getBenefitsBySection().get(rangeIdx - 1);
            }
        }
        return benefit;
    }

    // 이번 달 카드 혜택 업데이트
    public void updateUserCardBenefit(int userId, int benefitTemplateId, int receivedBenefitAmount){
        // 현재 연도와 월 추출
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();

        // 이번 달 기존 혜택 현황 조회 (없으면 예외 처리)
        UserCardBenefit existing = userCardBenefitRepository
                .findByUserIdAndBenefitTemplateIdAndYearAndMonth(userId, benefitTemplateId, currentYear, currentMonth);

        if (existing == null) {
            // 신규 생성: 최초 혜택 횟수는 1, 혜택 금액은 이번 결제 금액
            UserCardBenefit newBenefit = UserCardBenefit.builder()
                    .userId(userId)
                    .benefitTemplateId(benefitTemplateId)
                    // spendingTier는 필요에 따라 기본값을 설정하세요. 여기서는 0으로 설정합니다.
                    .spendingTier((short) 0)
                    .benefitCount((short) 1)
                    .benefitAmount(receivedBenefitAmount)
                    .updateDate(LocalDateTime.now())
                    .year(currentYear)
                    .month(currentMonth)
                    .build();
            userCardBenefitRepository.save(newBenefit);
        } else {
            // 기존 데이터 업데이트: benefit_count +1, benefit_amount에 이번 결제 금액 누적
            UserCardBenefit updatedBenefit = UserCardBenefit.builder()
                    .userCardBenefitId(existing.getUserCardBenefitId())
                    .userId(existing.getUserId())
                    .benefitTemplateId(existing.getBenefitTemplateId())
                    .spendingTier(existing.getSpendingTier())
                    .benefitCount((short) (existing.getBenefitCount() + 1))
                    .benefitAmount(existing.getBenefitAmount() + receivedBenefitAmount)
                    .updateDate(LocalDateTime.now())
                    .year(existing.getYear())
                    .month(existing.getMonth())
                    .build();
            userCardBenefitRepository.save(updatedBenefit);
        }
    }
}