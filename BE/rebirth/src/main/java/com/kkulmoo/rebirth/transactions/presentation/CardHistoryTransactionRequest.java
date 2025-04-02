package com.kkulmoo.rebirth.transactions.presentation;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CardHistoryTransactionRequest {
    private Integer cardId;
    private Integer page;
    private Integer pageSize;
    private Integer year;
    private Integer month;
}
