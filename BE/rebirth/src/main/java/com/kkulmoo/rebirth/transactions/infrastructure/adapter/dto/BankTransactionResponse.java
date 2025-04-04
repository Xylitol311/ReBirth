package com.kkulmoo.rebirth.transactions.infrastructure.adapter.dto;

import com.kkulmoo.rebirth.user.domain.UserId;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Builder(toBuilder = true)
@Setter
public class BankTransactionResponse {
    private UserId userId;
    private String accountNumber;  // 계좌번호
    private Long amount;  // 금액
    private String type;  // 입금, 이체, 체크카드
    private String description;  // 설명
    private LocalDateTime createdAt;
    private String approvalCode;  // 승인코드 (카드 결제 시에만 전달됨)

}