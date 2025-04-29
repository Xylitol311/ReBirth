package com.kkulmoo.rebirth.payment.presentation.response;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Builder
@ToString
public class PermanentTokenResponseByCardsaDTO {

    String cardUniqueNumber;
    String token;
    LocalDateTime createdAt;


}
