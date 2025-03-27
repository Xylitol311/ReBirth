package com.cardissuer.cardissuer.cards;


import com.cardissuer.cardissuer.user.CardTransactionDTO;
import com.cardissuer.cardissuer.user.CreateTransactionRequestDTO;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    @GetMapping("/test")
    public ResponseEntity<?> test(@RequestParam CreateTransactionRequestDTO createTransactionRequestDTO){

        System.out.println(createTransactionRequestDTO);
        CardTransactionDTO cardTransactionDTO = CardTransactionDTO.builder().createdAt("2025-03-26").response("yes").build();
        return ResponseEntity.ok(cardTransactionDTO);
    }



}
