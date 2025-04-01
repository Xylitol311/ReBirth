package com.cardissuer.cardissuer.transaction.presentation.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class CardTransactionRequest {
    private String userCI;
    private String cardUniqueNumber;
    private LocalDateTime fromDate;
}