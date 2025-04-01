package com.cardissuer.cardissuer.transaction.presentation;

import com.cardissuer.cardissuer.transaction.application.CardTransactionService;
import com.cardissuer.cardissuer.transaction.infrastrucuture.BankTransactionResponseDTO;
import com.cardissuer.cardissuer.transaction.presentation.dto.CardTransactionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class CardTransactionController {
    private final CardTransactionService cardTransactionService;

    // 넣을 때는 사용자가 누구인지 굳이 알려줄 필요가 없어요~
    // todo: BankTransactionResponseDTO 바꾸기... 혜택내역 혜택내용 받기
    @PostMapping
    public ResponseEntity<BankTransactionResponseDTO> createTransaction(
        @RequestBody CreateTransactionRequest createTransactionRequest) {

        System.out.println(createTransactionRequest.getToken());
        BankTransactionResponseDTO cardTransaction = cardTransactionService.createTransaction(createTransactionRequest);
        return new ResponseEntity<>(cardTransaction, HttpStatus.CREATED);
    }

    @PostMapping("/getMyData")
    public ResponseEntity<?> getMyCardTransactions(CardTransactionRequest cardTransactionRequest) {
        LocalDateTime localDateTime = cardTransactionRequest.getFromDate();
        Timestamp timestamp = Timestamp.valueOf(localDateTime);
        return ResponseEntity.ok(cardTransactionService.getTransactionsByUserCIAndCardUniqueNumberAfterTimestamp(
                cardTransactionRequest.getUserCI(),
                cardTransactionRequest.getCardUniqueNumber(),
                timestamp
        ));
    }
}
