package com.kkulmoo.rebirth.payment.presentation.response;

import lombok.Builder;
import lombok.Getter;

import javax.smartcardio.Card;
import java.time.LocalDateTime;

@Getter
@Builder
public class PermanentTokenResponseByCardsaDTO {

    String cardUniqueNumber;
    String token;
    LocalDateTime createdAt;
    Boolean isActive;
    Card card;
}
