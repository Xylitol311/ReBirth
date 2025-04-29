package com.kkulmo.bank.transactions.dto;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 거래 내역 조회 요청을 위한 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionHistoryRequestDTO {
	private String userCI;              // 사용자 키
	private String accountNumber;        // 계좌번호
	private LocalDateTime timestamp;     // 조회 시간 기준
}