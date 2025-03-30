package com.cardissuer.cardissuer.transaction.infrastrucuture;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.cardissuer.cardissuer.transaction.application.bankTransactionCreateDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BankAPI {
	private final WebClient bankAPIClient;

	public BankTransactionResponseDTO createTransaction(bankTransactionCreateDTO dto) {
		return bankAPIClient.post()
			.uri("/api/transactions")
			.bodyValue(dto)
			.retrieve()
			.bodyToMono(BankTransactionResponseDTO.class)
			.block(); // 동기식 호출
	}
}