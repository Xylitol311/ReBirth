package com.cardissuer.cardissuer.user;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CardTransactionDTO {

    String createdAt;
    String response;

}