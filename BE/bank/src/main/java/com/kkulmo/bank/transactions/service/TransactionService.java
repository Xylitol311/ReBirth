package com.kkulmo.bank.transactions.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kkulmo.bank.account.repository.AccountEntity;
import com.kkulmo.bank.account.service.AccountService;
import com.kkulmo.bank.transactions.dto.TransactionDTO;
import com.kkulmo.bank.transactions.dto.TransactionType;
import com.kkulmo.bank.transactions.repository.TransactionEntity;
import com.kkulmo.bank.transactions.repository.TransactionMapper;
import com.kkulmo.bank.transactions.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {

	private final TransactionRepository transactionRepository;
	private final AccountService accountService;
	private final TransactionMapper transactionMapper;

	// 트랜잭션 생성
	@Transactional
	public TransactionDTO createTransaction(TransactionDTO transactionDTO) {

		// 계좌를 비관적 락으로 가져옴
		AccountEntity account = accountService.getAccountWithPessimisticLock(
			transactionDTO.getUserId(), transactionDTO.getAccountNumber());

		String typeStr = transactionDTO.getType();
		if (typeStr == null || typeStr.isEmpty()) {
			throw new RuntimeException("거래 유형이 설정되지 않았습니다");
		}

		TransactionType type;
		try {
			type = TransactionType.valueOf(typeStr);
			System.out.println(typeStr);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("유효하지 않은 거래 유형입니다: " + typeStr);
		}

		switch (type) {
			case DEP:
				// 입금 처리
				processDeposit(transactionDTO, account);
				break;
			case TXN:
				// 카드 결제 처리
				processCardTransaction(transactionDTO, account);
				break;
			default:
				throw new RuntimeException("지원하지 않는 거래 유형입니다: " + type);
		}

		if(transactionDTO.getType().equals("REJ")){
			return transactionDTO;
		}

		TransactionEntity savedTransaction = transactionRepository.save(transactionMapper.toEntity(transactionDTO));
		// 계좌 잔액 업데이트 - 계좌번호로 직접 업데이트
		accountService.updateBalance(transactionDTO.getAccountNumber(), transactionDTO.getAmount());
		return transactionMapper.toDTO(savedTransaction);
	}

	/**
	 * 입금 처리
	 */
	private void processDeposit(TransactionDTO transactionDTO, AccountEntity account) {
		// 승인코드 생성
		String approvalCode = generateApprovalCode("DEP");
		transactionDTO.setApprovalCode(approvalCode);

		// 입금은 항상 양수 금액이어야 함
		if (transactionDTO.getAmount() <= 0) {
			throw new RuntimeException("입금 금액은 0보다 커야 합니다");
		}
	}

	private void processCardTransaction(TransactionDTO transactionDTO, AccountEntity account) {

		if (transactionDTO.getAmount() > account.getBalance()) {
			// 카드 거절 처리로 전환
			transactionDTO.setType("REJ");
			transactionDTO.setDescription("잔액 부족으로 카드 거래 거절: 필요 금액 " +
				Math.abs(transactionDTO.getAmount()) + ", 현재 잔액 " + account.getBalance());

			// 새로운 승인코드 생성
			String rejectionCode = generateApprovalCode("REJ");
			transactionDTO.setApprovalCode(rejectionCode);

			return;
		}

		transactionDTO.setAmount(transactionDTO.getAmount() * -1);
		transactionDTO.setApprovalCode(generateApprovalCode("TXN"));
		System.out.println(transactionDTO.getType());
	}

	/**
	 * 특정 계좌번호에 대해 특정 시간 이후의 거래 내역을 조회한다.
	 * 마이데이터를 사용하기 위해 만든거야. 계좌별 내역을 가져오는게 이득일듯?
	 *
	 * @param userKey 사용자 키
	 * @param accountNumber 계좌번호
	 * @param timestamp 조회 시작 시간
	 * @return 조회된 거래 내역 목록
	 */
	public List<TransactionDTO> getTransactionsByAccountNumberAndAfterTimestamp(
		String userKey, String accountNumber, LocalDateTime timestamp) {

		// 사용자 권한 검증 (해당 사용자의 계좌인지 확인)
		if (!accountService.validateAccountOwnership(userKey, accountNumber)) {
			throw new RuntimeException("해당 계좌에 권한이 없습니다.");
		}

		// 특정 시간 이후의 거래 내역 조회
		return transactionRepository.findByAccountNumberAndCreatedAtAfterOrderByCreatedAtDesc(
				accountNumber, timestamp).stream()
			.map(transactionMapper::toDTO)
			.collect(Collectors.toList());
	}

	private String generateApprovalCode(String type) {
		String prefix;
		if ("DEP".equals(type)) {
			prefix = "DEP";
		} else if ("TRF".equals(type)) {
			prefix = "TRF";
		} else if ("TXN".equals(type)) {
			prefix = "TXN";
		} else {
			prefix = "REJ";
		}

		// 현재 시간 밀리초 + 랜덤 숫자 4자리
		long timestamp = System.currentTimeMillis();
		int random = (int)(Math.random() * 10000);
		return prefix + timestamp + String.format("%04d", random);
	}
}