package com.cardissuer.cardissuer.transaction.infrastrucuture;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankTransactionResponseDTO {
	private Long amount;  // 금액
	private LocalDateTime createdAt;
	private String approvalCode;  // 승인코드 (카드 취소에도 전달 되도록)
}
