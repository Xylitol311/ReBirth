package com.cardissuer.cardissuer.transaction;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class CardTransactionService {
	private final CardTransactionRepository cardTransactionRepository;

	/**
	 * 새로운 카드 거래내역을 생성합니다.
	 *
	 * @param cardTransaction 생성할 거래내역 정보
	 * @return 생성된 거래내역 정보
	 */
	@Transactional
	public CardTransactionEntity createTransaction(CardTransactionEntity cardTransaction) {
		return cardTransactionRepository.save(cardTransaction);
	}

	/**
	 * 특정 사용자의 특정 시간 이후 카드 거래내역을 조회합니다.
	 *
	 * @param userId 조회할 사용자 ID
	 * @param timestamp 조회 시작 시간
	 * @return 사용자의 특정 시간 이후 카드 거래내역 목록
	 */
	@Transactional(readOnly = true)
	public List<CardTransactionEntity> getTransactionsByUserIdAfterTimestamp(Integer userId, Timestamp timestamp) {
		return cardTransactionRepository.findByCard_UserIdAndCreatedAtAfterOrderByCreatedAtDesc(userId, timestamp);
	}

	/**
	 * 특정 사용자의 모든 카드 거래내역을 조회합니다.
	 *
	 * @param userId 조회할 사용자 ID
	 * @return 사용자의 모든 카드 거래내역 목록
	 */
	@Transactional(readOnly = true)
	public List<CardTransactionEntity> getAllTransactionsByUserId(Integer userId) {
		// 과거 모든 거래내역을 가져오려면 매우 오래된 timestamp 사용
		Timestamp oldTimestamp = Timestamp.valueOf("1970-01-01 00:00:00");
		return cardTransactionRepository.findByCard_UserIdAndCreatedAtAfterOrderByCreatedAtDesc(userId, oldTimestamp);
	}
}