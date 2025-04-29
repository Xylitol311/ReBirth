package com.kkulmo.bank.transactions.dto;

import lombok.Getter;

@Getter
public enum TransactionType {
	DEP, // 입금 (Deposit)
	TRF, // 이체 (Transfer)
	TXN;  // 카드 (Check Card Transaction)
}