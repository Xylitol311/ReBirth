package com.kkulmo.bank.transactions.dto;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
	private String id;

	// 클라이언트로부터 전달받는 필드
	private String accountNumber;  // 계좌번호
	private Long amount;  // 금액
	private String userId;
	private String type;  // 입금, 이체, 체크카드
	private String description;  // 설명
	private LocalDateTime createdAt;
	private String approvalCode;  // 승인코드 (카드 결제 시에만 전달됨)

}