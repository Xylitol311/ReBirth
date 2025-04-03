package com.kkulmoo.rebirth.payment.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@AllArgsConstructor
@ToString
public class MyCardDto {
    private Integer cardId;
    private Integer cardTemplateId;
    private String permanentToken;
    private Short spendingTier;
}
