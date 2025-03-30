package com.cardissuer.cardissuer.user;

import lombok.Builder;
import lombok.Getter;


import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter
@Builder
public class CardTransactionDTO {

    private Timestamp createdAt;
    private String approvalCode;

}