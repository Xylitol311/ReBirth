package com.kkulmo.bank.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class AccountBalanceDTO {
	private String accountNumber;
	private Long balance;
}