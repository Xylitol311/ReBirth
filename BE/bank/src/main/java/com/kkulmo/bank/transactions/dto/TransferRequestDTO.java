package com.kkulmo.bank.transactions.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequestDTO {
	private String userKey;                   // 사용자 식별 키
	private String sourceAccountNumber;       // 출금 계좌번호
	private String destinationAccountNumber;  // 입금 계좌번호
	private Long amount;                      // 이체 금액
	private String description;               // 이체 설명(옵션)
}