package com.kkulmoo.rebirth.payment.presentation.response;

import lombok.Builder;
import lombok.Getter;
import java.sql.Timestamp;
import java.time.LocalDateTime;

// 결지 시 카드사로부터 받는 객체

@Getter
@Builder
public class CardTransactionDTO {
    private Long amount;  // 금액
    private LocalDateTime createdAt;
    private String approvalCode;  // 승인코드 (카드 취소에도 전달 되도록)

}