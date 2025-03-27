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

		// TransactionType Enum 값 가져오기
		String typeStr = transactionDTO.getType();
		if (typeStr == null || typeStr.isEmpty()) {
			throw new RuntimeException("거래 유형이 설정되지 않았습니다");
		}

		TransactionType type;
		try {
			type = TransactionType.valueOf(typeStr);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("유효하지 않은 거래 유형입니다: " + typeStr);
		}

		// 출금 거래(이체, 카드)인 경우에만 잔액 검증
		if (type != TransactionType.DEP && Math.abs(transactionDTO.getAmount()) > account.getBalance()) {
			throw new RuntimeException("계좌 잔액이 부족합니다: " + transactionDTO.getAccountNumber());
		}

		// 승인코드 처리: 카드가 아닌 경우 자체 생성
		if (type != TransactionType.TXN &&
			(transactionDTO.getApprovalCode() == null || transactionDTO.getApprovalCode().isEmpty())) {
			// 승인코드 생성 (시간 + 랜덤값)
			String approvalCode = generateApprovalCode(type.name());
			transactionDTO.setApprovalCode(approvalCode);
		}

		TransactionEntity transactionEntity = transactionMapper.toEntity(transactionDTO);
		TransactionEntity savedTransaction = transactionRepository.save(transactionEntity);

		// 계좌 잔액 업데이트 - 계좌번호로 직접 업데이트
		accountService.updateBalance(transactionDTO.getAccountNumber(), transactionDTO.getAmount());

		return transactionMapper.toDTO(savedTransaction);
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

	// @Transactional
	// public TransactionDTO transferMoney(TransferRequestDTO transferRequestDTO) {
	// 	// 사용자 권한 검증
	// 	if (!accountService.validateAccountOwnership(transferRequestDTO.getUserKey(),
	// 		transferRequestDTO.getSourceAccountNumber())) {
	// 		throw new RuntimeException("이 계좌에 대한 접근권한이 없습니다.");
	// 	}
	//
	// 	// 출금 계좌에서 금액 차감
	// 	accountService.updateBalance(transferRequestDTO.getSourceAccountNumber(), -transferRequestDTO.getAmount());
	//
	// 	// 입금 계좌에 금액 추가
	// 	accountService.updateBalance(transferRequestDTO.getDestinationAccountNumber(), transferRequestDTO.getAmount());
	//
	// 	// 승인 코드 생성
	// 	String approvalCode = generateApprovalCode("이체");
	// 	LocalDateTime now = LocalDateTime.now();
	//
	// 	// 거래 기록 저장 - 출금 계좌
	// 	TransactionEntity transaction = TransactionEntity.builder()
	// 		.accountNumber(transferRequestDTO.getSourceAccountNumber())
	// 		.amount(-transferRequestDTO.getAmount())
	// 		.type("이체")
	// 		.createdAt(now)
	// 		.description("계좌이체 - " + transferRequestDTO.getDestinationAccountNumber() + "로 송금")
	// 		.approvalCode(approvalCode)
	// 		.build();
	//
	// 	TransactionEntity savedTransaction = transactionRepository.save(transaction);
	//
	// 	// 수신 계좌의 거래 기록도 저장
	// 	TransactionEntity recipientTransaction = TransactionEntity.builder()
	// 		.accountNumber(transferRequestDTO.getDestinationAccountNumber())
	// 		.amount(transferRequestDTO.getAmount())
	// 		.type("입금")
	// 		.createdAt(now)
	// 		.description("계좌이체 - " + transferRequestDTO.getSourceAccountNumber() + "에서 입금")
	// 		.approvalCode(approvalCode)  // 같은 승인코드 사용
	// 		.build();
	//
	// 	transactionRepository.save(recipientTransaction);
	//
	// 	return convertToDTO(savedTransaction);
	// }
	//

	// 승인코드 생성 메서드
	private String generateApprovalCode(String type) {
		String prefix;
		if ("DEP".equals(type)) {
			prefix = "DEP";
		} else if ("TRF".equals(type)) {
			prefix = "TRF";
		} else {
			prefix = "TXN";
		}

		// 현재 시간 밀리초 + 랜덤 숫자 4자리
		long timestamp = System.currentTimeMillis();
		int random = (int) (Math.random() * 10000);
		return prefix + timestamp + String.format("%04d", random);
	}

}


