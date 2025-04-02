package com.kkulmoo.rebirth.transactions.presentation;

import com.kkulmoo.rebirth.common.ApiResponseDTO.ApiResponseDTO;
import com.kkulmoo.rebirth.transactions.application.TransactionService;
import com.kkulmoo.rebirth.transactions.application.dto.TransactionHistoryResponseData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping("/card/history")
    public ResponseEntity<ApiResponseDTO<TransactionHistoryResponseData>> getCardTransactionHistory(
            @RequestBody CardHistoryTransactionRequest request) {
        return ResponseEntity.ok(ApiResponseDTO.success("조회 성공",
                transactionService.getCardTransactionHistory(request)));
    }

}