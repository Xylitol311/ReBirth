package com.kkulmoo.rebirth.card.presentation.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CardOrderRequest {
    private Integer cardId;
    private Short position;
}