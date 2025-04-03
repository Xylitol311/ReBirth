package com.kkulmo.bank.account.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.kkulmo.bank.account.dto.AccountBalanceDTO;
import com.kkulmo.bank.account.dto.AccountDTO;
import com.kkulmo.bank.account.repository.AccountEntity;
import com.kkulmo.bank.account.repository.AccountRepository;
import com.kkulmo.bank.common.AccountNotFoundException;
import com.kkulmo.bank.user.dto.User;
import com.kkulmo.bank.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {

	private final AccountRepository accountRepository;
	private final UserRepository userRepository;

	//계좌 생성.
	public AccountDTO createAccount(AccountDTO accountDTO) {
		// 1. 사용자 ID 유효성 검증
		String userId = accountDTO.getUserId();
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. ID: " + userId));

		System.out.println(accountDTO.getAccountNumber());
		// 3. 계좌 엔티티 생성
		AccountEntity account = AccountEntity.builder()
			.accountNumber(accountDTO.getAccountNumber())
			.userId(userId)
			// 설정
			.createdAt(accountDTO.getCreatedAt() != null ? accountDTO.getCreatedAt() : LocalDateTime.now())
			.build();

		// 4. 계좌 저장
		AccountEntity savedAccount = accountRepository.save(account);

		// 5. DTO 변환 후 반환
		return convertToDTO(savedAccount);
	}


	public boolean validateAccountOwnership(String userKey, String accountNumber) {
		// 계좌번호로 계좌 조회
		AccountEntity account = accountRepository.findById(accountNumber)
			.orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));

		// 계좌의 소유자 userId와 전달받은 userKey가 일치하는지 확인
		return account.getUserId().equals(userKey);
	}

	public AccountBalanceDTO getBalanceByUserIdAndAccountNumber(String userId, String accountNumber) {
		AccountEntity accountEntity = accountRepository.findByUserIdAndAccountNumber(userId, accountNumber)
			.orElseThrow(() -> new AccountNotFoundException("계좌를 찾을 수 없습니다."));

		return AccountBalanceDTO.builder()
			.accountNumber(accountEntity.getAccountNumber())
			.balance(accountEntity.getBalance())
			.build();
	}

	public void updateBalance(String accountId, Long amount) {
		AccountEntity account = accountRepository.findById(accountId)
			.orElseThrow(() -> new RuntimeException("Account not found with id: " + accountId));

		account.updateBalance(amount);
		accountRepository.save(account);
	}

	public AccountEntity getAccountWithPessimisticLock(String userId, String accountNumber) {
		return accountRepository.findByUserIdAndAccountNumberWithPessimisticLock(userId, accountNumber)
			.orElseThrow(() -> new RuntimeException("계좌를 찾을 수 없거나 접근 권한이 없습니다."));
	}

	private AccountDTO convertToDTO(AccountEntity account) {
		return AccountDTO.builder()
			.accountNumber(account.getAccountNumber())
			.userId(account.getUserId())
			.createdAt(account.getCreatedAt())
			.build();
	}

	private AccountEntity convertToEntity(AccountDTO accountDTO) {
		return AccountEntity.builder()
			.accountNumber(accountDTO.getAccountNumber())
			.userId(accountDTO.getUserId())
			.build();
	}
}