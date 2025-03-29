package com.kkulmo.bank.account.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kkulmo.bank.account.dto.AccountDTO;
import com.kkulmo.bank.account.service.AccountService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

	private final AccountService accountService;

	// UserId와 계좌번호로 특정 계좌 잔액만 String으로 가져오기
	@GetMapping("/user/{userId}/account/{accountNumber}/balance")
	public ResponseEntity<Long> getAccountBalanceByUserIdAndAccountNumber(
		@PathVariable String userId,
		@PathVariable String accountNumber) {
		Long balance = accountService.getBalanceByUserIdAndAccountNumber(userId, accountNumber).getBalance();
		return ResponseEntity.ok(balance);
	}

	//계좌 만들기
	@PostMapping
	public ResponseEntity<AccountDTO> createAccount(@RequestBody AccountDTO accountDTO) {
		return ResponseEntity.ok(accountService.createAccount(accountDTO));
	}

}
