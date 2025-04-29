package com.kkulmoo.rebirth.transactions.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Pageable;

@AllArgsConstructor
@Getter
public class CardTransactionQueryParams {
    private final String cardUniqueNumber;
    private final Integer year;
    private final Integer month;
    private final Pageable pageable;
}
