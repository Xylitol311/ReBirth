package com.kkulmo.bank.transactions.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kkulmo.bank.transactions.dto.TransactionDTO;
import com.kkulmo.bank.transactions.dto.TransactionHistoryRequestDTO;
import com.kkulmo.bank.transactions.service.TransactionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;


    /**
     * 거래 내역 생성 API
     *
     * @param transactionDTO 거래 정보 (userKey, accountNumber, amount, type 등)
     * @return 생성된 거래 정보
     */
    @PostMapping
    public ResponseEntity<TransactionDTO> createTransaction(@RequestBody TransactionDTO transactionDTO) {
        System.out.println("거래내역 생성중입니다.");
        return ResponseEntity.ok(transactionService.createTransaction(transactionDTO));
    }

    /**
     * 계좌별 거래 내역 조회 API
     * 특정 시간 이후의 모든 거래 기록을 가져옴
     *
     * @param requestDTO userKey, accountNumber, timestamp 정보
     * @return 조회된 거래 내역 목록
     */
    @PostMapping("/history")
    public ResponseEntity<List<TransactionDTO>> getTransactionHistory(
            @RequestBody TransactionHistoryRequestDTO requestDTO) {
        return ResponseEntity.ok(transactionService.getTransactionsByAccountNumberAndAfterTimestamp(requestDTO.getUserKey(),
                requestDTO.getAccountNumber(),
                requestDTO.getTimestamp())
        );
    }
}