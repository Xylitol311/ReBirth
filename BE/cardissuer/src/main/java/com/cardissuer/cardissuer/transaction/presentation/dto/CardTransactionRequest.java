package com.cardissuer.cardissuer.transaction.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@ToString
public class CardTransactionRequest {
    private String userCI;
    private String cardUniqueNumber;
    private LocalDateTime fromDate;
}