package com.kkulmoo.rebirth.transactions.application.dto;

import com.kkulmoo.rebirth.card.domain.MyCards;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CardTransactionRequest {
    private String userCI;
    private List<MyCards> cards;
}