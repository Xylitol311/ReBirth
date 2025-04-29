package com.kkulmoo.rebirth.transactions.domain;

import com.kkulmoo.rebirth.user.domain.UserId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Transactions {
    Integer TransactionsId;
    UserId userId;
    LocalDateTime createdAt;
    String ApprovalNumber;
}
