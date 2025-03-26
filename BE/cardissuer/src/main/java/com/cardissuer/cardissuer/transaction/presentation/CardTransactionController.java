package com.cardissuer.cardissuer.transaction.presentation;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cardissuer.cardissuer.cards.application.CardResponse;
import com.cardissuer.cardissuer.transaction.domain.CardTransaction;
import com.cardissuer.cardissuer.transaction.infrastrucuture.CardTransactionEntity;
import com.cardissuer.cardissuer.transaction.application.CardTransactionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class CardTransactionController {
	private final CardTransactionService cardTransactionService;

	// 넣을 때는 사용자가 누구인지 굳이 알려줄 필요가 없어요~
	@PostMapping
	public ResponseEntity<CardTransaction> createTransaction(
		@RequestBody CreateTransactionRequest createTransactionRequest) {
		CardTransaction cardTransaction = cardTransactionService.createTransaction(createTransactionRequest);
		return new ResponseEntity<>(cardTransaction, HttpStatus.CREATED);
	}

	//사용자의 거래내역을 가져와라
	@GetMapping("/user")
	public ResponseEntity<List<CardTransactionEntity>> getAllTransactionsByUserApiKey(
		@RequestHeader("Authorization") String authorizationHeader) {
		// "Bearer " 접두사 제거하여 실제 API 키 추출
		String ssafyAPIKey = authorizationHeader.replace("Bearer ", "");
		List<CardTransactionEntity> transactions = cardTransactionService.getAllTransactionsByUserApiKey(ssafyAPIKey);
		return new ResponseEntity<>(transactions, HttpStatus.OK);
	}

	@GetMapping("/user/after")
	public ResponseEntity<List<CardTransactionEntity>> getTransactionsByUserApiKeyAfterTimestamp(
		@RequestHeader("Authorization") String authorizationHeader,
		@RequestParam String timestamp) {
		// "Bearer " 접두사 제거하여 실제 API 키 추출
		String ssafyAPIKey = authorizationHeader.replace("Bearer ", "");

		// String 타입의 timestamp를 Timestamp 객체로 변환
		Timestamp startTime = Timestamp.valueOf(timestamp.replace('T', ' '));

		List<CardTransactionEntity> transactions = cardTransactionService.getTransactionsByUserApiKeyAfterTimestamp(
			ssafyAPIKey, startTime);
		return new ResponseEntity<>(transactions, HttpStatus.OK);
	}
}
