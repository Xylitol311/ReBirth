package com.cardissuer.cardissuer.transaction;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class CardTransactionController {
	private final CardTransactionService cardTransactionService;

	/**
	 * 새로운 카드 거래내역을 등록합니다.
	 *
	 * @param cardTransaction 등록할 거래내역 정보
	 * @return ResponseEntity 객체
	 */
	@PostMapping
	public ResponseEntity<CardTransactionEntity> createTransaction(@RequestBody CardTransactionEntity cardTransaction) {
		CardTransactionEntity createdTransaction = cardTransactionService.createTransaction(cardTransaction);
		return new ResponseEntity<>(createdTransaction, HttpStatus.CREATED);
	}

	/**
	 * 특정 사용자의 모든 카드 거래내역을 조회합니다.
	 *
	 * @param userId 조회할 사용자 ID
	 * @return ResponseEntity 객체
	 */
	@GetMapping("/user/{userId}")
	public ResponseEntity<List<CardTransactionEntity>> getAllTransactionsByUserId(
		@PathVariable Integer userId) {
		List<CardTransactionEntity> transactions = cardTransactionService.getAllTransactionsByUserId(userId);
		return new ResponseEntity<>(transactions, HttpStatus.OK);
	}

	/**
	 * 특정 사용자의 특정 시간 이후 카드 거래내역을 조회합니다.
	 *
	 * @param userId 조회할 사용자 ID
	 * @param timestamp 조회 시작 시간 (ISO 형식: yyyy-MM-dd'T'HH:mm:ss)
	 * @return ResponseEntity 객체
	 */
	@GetMapping("/user/{userId}/after")
	public ResponseEntity<List<CardTransactionEntity>> getTransactionsByUserIdAfterTimestamp(
		@PathVariable Integer userId,
		@RequestParam String timestamp) {
		// String 타입의 timestamp를 Timestamp 객체로 변환
		Timestamp startTime = Timestamp.valueOf(timestamp.replace('T', ' '));
		List<CardTransactionEntity> transactions = cardTransactionService.getTransactionsByUserIdAfterTimestamp(userId, startTime);
		return new ResponseEntity<>(transactions, HttpStatus.OK);
	}
}
