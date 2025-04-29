package com.kkulmoo.rebirth.transactions.application.dto;

import com.kkulmoo.rebirth.transactions.presentation.PaginationDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionHistoryResponseData {
    private List<TransactionRecord> transactionHistory;
    private PaginationDto pagination;
}
