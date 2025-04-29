package com.kkulmoo.rebirth.card.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardResponse {
    private Integer cardId;
    private String cardImgUrl;
    private String cardName;
    private Integer totalSpending;
    private Integer maxSpending;
    private List<Integer> performanceRange;
    private Integer receivedBenefitAmount;
    private Short currentTier;
    private Integer maxBenefitAmount;
}
