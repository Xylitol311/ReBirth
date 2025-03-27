package com.cardissuer.cardissuer.cards;


import com.cardissuer.cardissuer.user.CardTransactionDTO;
import com.cardissuer.cardissuer.user.CreateTransactionRequestDTO;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/cards")@RequiredArgsConstructor
public class CardController {

    @PostMapping("/test")
    public ResponseEntity<?> test(@RequestBody CreateTransactionRequestDTO createTransactionRequestDTO){

        System.out.println("여기 도착을 했나?");

        String now = "2023-04-09 02:36:30.0";
        Timestamp ts = Timestamp.valueOf(now);
        System.out.println(createTransactionRequestDTO);
        CardTransactionDTO cardTransactionDTO = CardTransactionDTO.builder().createdAt(ts).approvalCode("승인완료").build();
        return ResponseEntity.ok(cardTransactionDTO);
    }



}
