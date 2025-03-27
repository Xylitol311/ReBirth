package com.cardissuer.cardissuer.cards;


import com.cardissuer.cardissuer.user.CardTransactionDTO;
import com.cardissuer.cardissuer.user.CreateTransactionRequestDTO;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/cards")@RequiredArgsConstructor
public class CardController {

    @PostMapping("/test")
    public ResponseEntity<?> test(@RequestBody CreateTransactionRequestDTO createTransactionRequestDTO) {


        // 현재 시간으로 Timestamp 생성
        Timestamp ts = Timestamp.from(Instant.now());

        CardTransactionDTO cardTransactionDTO = CardTransactionDTO.builder()
                .createdAt(ts)
                .approvalCode("승인완료")
                .build();

        return ResponseEntity.ok(cardTransactionDTO);
    }



}
