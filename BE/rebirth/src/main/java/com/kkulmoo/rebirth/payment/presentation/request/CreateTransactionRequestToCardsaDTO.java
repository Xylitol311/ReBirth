package com.kkulmoo.rebirth.payment.presentation.request;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

//결제시 카드사에게 보내는 정보
@Getter
@Builder
public class CreateTransactionRequestToCardsaDTO {
    String permanentToken;
    int amount;
    String merchantName;
    //discount, mileage 2개중 하나
    String benefitType;
    Integer benefitId;
    Integer benefitAmount;
    LocalDateTime createdAt;
}