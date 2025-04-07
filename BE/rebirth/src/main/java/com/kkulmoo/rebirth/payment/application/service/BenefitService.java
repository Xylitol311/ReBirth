package com.kkulmoo.rebirth.payment.application.service;

import com.kkulmoo.rebirth.card.domain.BenefitRepository;
import com.kkulmoo.rebirth.card.domain.CardRepository;
import com.kkulmoo.rebirth.card.domain.DiscountType;
import com.kkulmoo.rebirth.card.domain.MyCard;
import com.kkulmoo.rebirth.payment.application.BenefitInfo;
import com.kkulmoo.rebirth.payment.infrastructure.dto.MerchantJoinDto;
import com.kkulmoo.rebirth.payment.presentation.response.CalculatedBenefitDto;
import com.kkulmoo.rebirth.user.domain.UserCardBenefit;
import com.kkulmoo.rebirth.user.domain.UserId;
import com.kkulmoo.rebirth.user.domain.repository.UserCardBenefitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BenefitService {

    private final CardRepository cardRepository;
    private final BenefitRepository benefitRepository;
    private final UserCardBenefitRepository userCardBenefitRepository;

    // 추천 카드 혜택 계산 (모든 보유 카드에 대해 계산 후 최대 혜택 선택)
    public CalculatedBenefitDto recommendPaymentCard(Integer userId, int amount, MerchantJoinDto merchantJoinDto) {
        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();

        UserId userId1 = new UserId(userId);
        List<MyCard> myCards = cardRepository.findByUserId(userId1);
        log.info("추천 혜택 계산 - myCards count: {}", myCards.size());

        Queue<CalculatedBenefitDto> benefitQueue = new PriorityQueue<>(Comparator.comparingInt(CalculatedBenefitDto::getBenefitAmount).reversed());

        for (MyCard card : myCards) {
            if (card.getPermanentToken() == null) {
                log.debug("카드 id {}의 영구 토큰이 없음.", card.getCardId());
                continue;
            }
            log.debug("카드 id {} - 영구 토큰: {}", card.getCardId(), card.getPermanentToken());
            List<BenefitInfo> benefitInfos = benefitRepository.findBenefitsByMerchantFilter(
                    card.getCardTemplateId(),
                    merchantJoinDto.getCategoryId(),
                    merchantJoinDto.getSubCategoryId(),
                    merchantJoinDto.getMerchantId()
            );
            log.info("카드 id {} - 적용 가능한 혜택 count: {}", card.getCardId(), benefitInfos.size());

            for (BenefitInfo benefitInfo : benefitInfos) {
                if ("쿠폰".equals(benefitInfo.getBenefitType().toString())) {
                    log.debug("혜택 id {}는 쿠폰으로 제외.", benefitInfo.getBenefitId());
                    continue;
                }
                Optional<UserCardBenefit> optionalUserCardBenefit = userCardBenefitRepository.findByUserIdAndBenefitTemplateIdAndYearAndMonth(
                        userId,
                        benefitInfo.getBenefitId(),
                        currentYear,
                        currentMonth
                );
                UserCardBenefit userCardBenefit = optionalUserCardBenefit.orElseGet(() -> {
                    log.info("UserCardBenefit 없음 - userId: {}, benefitTemplateId: {}, year: {}, month: {}. 기본값 생성.",
                            userId, benefitInfo.getBenefitId(), currentYear, currentMonth);
                    return UserCardBenefit.builder()
                            .userId(userId)
                            .benefitTemplateId(benefitInfo.getBenefitId())
                            .year(currentYear)
                            .month(currentMonth)
                            .spendingTier((short) 0)
                            .benefitCount((short) 0)
                            .benefitAmount(0)
                            .updateDate(LocalDateTime.now())
                            .build();
                });

                int discountAmount = calculateBenefitAmount(benefitInfo, amount, userCardBenefit);
                log.info("카드 id {} - 혜택 id {}: discountAmount={}, spendingTier={}, benefitCount={}, benefitAmount={}",
                        card.getCardId(), benefitInfo.getBenefitId(), discountAmount,
                        userCardBenefit.getSpendingTier(), userCardBenefit.getBenefitCount(), userCardBenefit.getBenefitAmount());

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
            CalculatedBenefitDto bestBenefit = benefitQueue.poll();
            log.info("최종 추천 혜택 선택 - myCardId: {}, permanentToken: {}, benefitId: {}, benefitAmount: {}, benefitType: {}",
                    bestBenefit.getMyCardId(), bestBenefit.getPermanentToken(), bestBenefit.getBenefitId(),
                    bestBenefit.getBenefitAmount(), bestBenefit.getBenefitType());
            return bestBenefit;
        }
        log.error("추천 혜택 계산 실패 - 결과값 없음.");
        return null;
    }

    // 실제 카드 혜택 계산 (영구토큰을 기반으로 단일 카드에 대해 계산)
    public CalculatedBenefitDto calculateRealBenefit(int userId, int amount, MerchantJoinDto merchantJoinDto, MyCard myCard) {
        if (myCard.getPermanentToken() == null) {
            log.info("실제 혜택 계산 오류 - 카드 id {}의 영구 토큰이 없음.", myCard.getCardId());
        }
        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();

        List<BenefitInfo> benefitInfos = benefitRepository.findBenefitsByMerchantFilter(
                myCard.getCardTemplateId(),
                merchantJoinDto.getCategoryId(),
                merchantJoinDto.getSubCategoryId(),
                merchantJoinDto.getMerchantId()
        );
        log.info("실제 혜택 계산 - 카드 id {}에 적용 가능한 혜택 count: {}", myCard.getCardId(), benefitInfos.size());

        Queue<CalculatedBenefitDto> benefitQueue = new PriorityQueue<>(Comparator.comparingInt(CalculatedBenefitDto::getBenefitAmount).reversed());
        for (BenefitInfo benefitInfo : benefitInfos) {
            if ("쿠폰".equals(benefitInfo.getBenefitType().toString())) {
                log.debug("실제 혜택 계산 - 혜택 id {}는 쿠폰으로 제외.", benefitInfo.getBenefitId());
                continue;
            }
            Optional<UserCardBenefit> optionalUserCardBenefit = userCardBenefitRepository.findByUserIdAndBenefitTemplateIdAndYearAndMonth(
                    userId,
                    benefitInfo.getBenefitId(),
                    currentYear,
                    currentMonth
            );
            UserCardBenefit userCardBenefit = optionalUserCardBenefit.orElseGet(() -> {
                log.info("실제 혜택 계산 - UserCardBenefit 없음 - userId: {}, benefitTemplateId: {}, year: {}, month: {}. 기본값 생성.",
                        userId, benefitInfo.getBenefitId(), currentYear, currentMonth);
                return UserCardBenefit.builder()
                        .userId(userId)
                        .benefitTemplateId(benefitInfo.getBenefitId())
                        .year(currentYear)
                        .month(currentMonth)
                        .spendingTier((short) 0)
                        .benefitCount((short) 0)
                        .benefitAmount(0)
                        .updateDate(LocalDateTime.now())
                        .build();
            });

            int discountAmount = calculateBenefitAmount(benefitInfo, amount, userCardBenefit);
            log.info("실제 혜택 계산 - 카드 id {} - 혜택 id {}: discountAmount={}, spendingTier={}, benefitCount={}, benefitAmount={}",
                    myCard.getCardId(), benefitInfo.getBenefitId(), discountAmount,
                    userCardBenefit.getSpendingTier(), userCardBenefit.getBenefitCount(), userCardBenefit.getBenefitAmount());

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
            CalculatedBenefitDto bestBenefit = benefitQueue.poll();
            log.info("실제 혜택 계산 완료 - 선택된 혜택: myCardId: {}, permanentToken: {}, benefitId: {}, benefitAmount: {}, benefitType: {}",
                    bestBenefit.getMyCardId(), bestBenefit.getPermanentToken(), bestBenefit.getBenefitId(),
                    bestBenefit.getBenefitAmount(), bestBenefit.getBenefitType());
            return bestBenefit;
        }
        log.error("실제 혜택 계산 실패 - 결과값 없음.");
        return null;
    }

    // 할인 금액 계산 메서드 (사용자 카드 혜택 정보를 기반)
    public int calculateBenefitAmount(BenefitInfo benefitInfo, int amount, UserCardBenefit userCardBenefit) {
        if (userCardBenefit == null) {
            log.info("할인 금액 계산 오류 - UserCardBenefit 객체가 null");
            return 0;
        }
        if (userCardBenefit.getSpendingTier() == 0) {
            log.debug("할인 금액 계산 - spendingTier 0, 할인 없음.");
            return 0;
        }
        double benefit = 0.0;
        int spendingTier = userCardBenefit.getSpendingTier();
        if (benefitInfo.getBenefitConditionType() == 4 || benefitInfo.getBenefitConditionType() == 1) {
            if (benefitInfo.getBenefitsBySection() != null && benefitInfo.getBenefitsBySection().size() >= spendingTier) {
                log.debug("할인 금액 계산 - 단일 또는 구간 조건, spendingTier: {}", spendingTier);
                benefit = benefitInfo.getBenefitsBySection().get(spendingTier - 1);
            }
        }
        if (benefitInfo.getBenefitConditionType() == 2) {
            benefit = calculateBenefit(benefitInfo, amount);
        }
        if (benefitInfo.getBenefitConditionType() == 3 && spendingTier >= 1) {
            benefit = calculateBenefit(benefitInfo, amount);
        }
        if (benefit == 0) {
            log.debug("할인 금액 계산 - benefit 0 반환.");
            return 0;
        }
        if (benefitInfo.getBenefitUsageLimit() != null && benefitInfo.getBenefitUsageLimit().size() >= spendingTier) {
            if (userCardBenefit.getBenefitCount() >= benefitInfo.getBenefitUsageLimit().get(spendingTier - 1)) {
                log.debug("할인 금액 계산 - 사용 제한(횟수) 초과.");
                return 0;
            }
        }
        int totalAbleBenefitAmount = Integer.MAX_VALUE;
        if (benefitInfo.getBenefitUsageAmount() != null && benefitInfo.getBenefitUsageAmount().size() >= spendingTier) {
            totalAbleBenefitAmount = benefitInfo.getBenefitUsageAmount().get(spendingTier - 1);
        }
        if (userCardBenefit.getBenefitAmount() >= totalAbleBenefitAmount) {
            log.debug("할인 금액 계산 - 사용 제한(금액) 초과.");
            return 0;
        }
        int result;
        if (benefitInfo.getDiscountType() == DiscountType.AMOUNT) {
            result = Math.min((int) benefit, totalAbleBenefitAmount - userCardBenefit.getBenefitAmount());
        } else {
            result = Math.min((int) (amount * benefit), totalAbleBenefitAmount - userCardBenefit.getBenefitAmount());
        }
        log.debug("할인 금액 계산 완료 - result: {}", result);
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
        log.debug("구간 기준 할인 계산 - rangeIdx: {}, benefit: {}", rangeIdx, benefit);
        return benefit;
    }

    // 이번 달 카드 혜택 업데이트
    public void updateUserCardBenefit(int userId, int benefitTemplateId, int receivedBenefitAmount) {
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();

        Optional<UserCardBenefit> optionalExisting = userCardBenefitRepository
                .findByUserIdAndBenefitTemplateIdAndYearAndMonth(userId, benefitTemplateId, currentYear, currentMonth);

        if (optionalExisting.isEmpty()) {
            log.info("updateUserCardBenefit - 신규 생성: userId={}, benefitTemplateId={}, year={}, month={}, receivedBenefitAmount={}",
                    userId, benefitTemplateId, currentYear, currentMonth, receivedBenefitAmount);
            UserCardBenefit newBenefit = UserCardBenefit.builder()
                    .userId(userId)
                    .benefitTemplateId(benefitTemplateId)
                    .spendingTier((short) 0)
                    .benefitCount((short) 1)
                    .benefitAmount(receivedBenefitAmount)
                    .updateDate(LocalDateTime.now())
                    .year(currentYear)
                    .month(currentMonth)
                    .build();
            userCardBenefitRepository.save(newBenefit);
        } else {
            UserCardBenefit existing = optionalExisting.get();
            log.info("updateUserCardBenefit - 업데이트 전: userId={}, benefitTemplateId={}, benefitCount={}, benefitAmount={}",
                    existing.getUserId(), existing.getBenefitTemplateId(), existing.getBenefitCount(), existing.getBenefitAmount());
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
            log.info("updateUserCardBenefit - 업데이트 후: userId={}, benefitTemplateId={}, benefitCount={}, benefitAmount={}",
                    updatedBenefit.getUserId(), updatedBenefit.getBenefitTemplateId(), updatedBenefit.getBenefitCount(), updatedBenefit.getBenefitAmount());
            userCardBenefitRepository.save(updatedBenefit);
        }
    }
}
