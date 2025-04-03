package com.kkulmoo.rebirth.transactions.application.mapper;

import com.kkulmoo.rebirth.transactions.application.dto.TransactionHistoryResponseData;
import com.kkulmoo.rebirth.transactions.application.dto.TransactionRecord;
import com.kkulmoo.rebirth.transactions.presentation.PaginationDto;
import com.kkulmoo.rebirth.transactions.presentation.TransactionHistoryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TransactionHistoryMapper {

    public TransactionHistoryResponseData toResponseData(Slice<TransactionHistoryDto> transactionsSlice){
        List<TransactionRecord> transactionRecords = transactionsSlice.getContent().stream()
                .map(this::toTransactionRecord)
                .collect(Collectors.toList());

        // 페이지네이션 정보 생성
        PaginationDto pagination = PaginationDto.builder()
                .currentPage(transactionsSlice.getNumber())
                .pageSize(transactionsSlice.getSize())
                .hasMore(transactionsSlice.hasNext())
                .build();

        // 응답 데이터 생성 및 반환
        return TransactionHistoryResponseData.builder()
                .transactionHistory(transactionRecords)
                .pagination(pagination)
                .build();
    }
    private TransactionRecord toTransactionRecord(TransactionHistoryDto dto) {
        return TransactionRecord.builder()
                .transactionDate(dto.getTransactionDate())
                .transactionCategory(dto.getTransactionCategoryName()) // 필드명 차이 주의
                .spendingAmount(dto.getSpendingAmount())
                .merchantName(dto.getMerchantName())
                .receivedBenefitAmount(dto.getReceivedBenefitAmount())
                .build();
    }
}
