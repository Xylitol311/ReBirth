package com.kkulmo.bank.account.dto;

import java.time.LocalDateTime;

import jakarta.persistence.GeneratedValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
public class AccountDTO {
	private String accountNumber;
	private String userId;
	private Long balance;
	private LocalDateTime createdAt;
}