package com.kkulmoo.rebirth.card.application.dto;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
@Builder
public class CardDetailResponse {
    Integer cardId;
    String cardImageUrl;
    String cardName;
    Integer maxPerformanceAmount;
    Integer currentPerformanceAmount;
    Short spendingMaxTier;
    Short currentSpendingTier;
    Short lastMonthPerformance;
    Integer amountRemainingNext;
    List<Integer> performanceRange;
    List<CardBenefit> cardBenefits;
}
