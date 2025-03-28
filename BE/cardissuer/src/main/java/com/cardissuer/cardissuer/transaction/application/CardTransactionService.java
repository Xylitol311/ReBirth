package com.cardissuer.cardissuer.transaction.application;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import com.cardissuer.cardissuer.cards.domain.Card;
import com.cardissuer.cardissuer.cards.domain.CardRepository;
import com.cardissuer.cardissuer.cards.domain.CardUniqueNumber;
import com.cardissuer.cardissuer.cards.domain.PermanentToken;
import com.cardissuer.cardissuer.cards.infrastructure.PermanentTokenEntity;
import com.cardissuer.cardissuer.common.exception.UserNotFoundException;
import com.cardissuer.cardissuer.transaction.domain.CardTransaction;
import com.cardissuer.cardissuer.transaction.domain.CardTransactionRepository;
import com.cardissuer.cardissuer.transaction.infrastrucuture.BankAPI;
import com.cardissuer.cardissuer.transaction.infrastrucuture.BankTransactionResponseDTO;
import com.cardissuer.cardissuer.transaction.infrastrucuture.CardTransactionEntity;
import com.cardissuer.cardissuer.transaction.presentation.CreateTransactionRequest;
import com.cardissuer.cardissuer.user.domain.User;
import com.cardissuer.cardissuer.user.domain.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CardTransactionService {
	private final CardTransactionRepository cardTransactionRepository;
	private final UserRepository userRepository;
	private final CardRepository cardRepository;
	private final BankAPI bankAPI;

	@Transactional
	public BankTransactionResponseDTO createTransaction(CreateTransactionRequest createTransactionRequest) {

		System.out.println("서비스에서 토큰");
		System.out.println(createTransactionRequest.getToken());
		// TODO: 토큰을 가지고 사용자 보유카드 가져오기.

		PermanentToken token = cardRepository.findTokenByToken(createTransactionRequest.getToken())
			.orElseThrow(() -> new RuntimeException("유효하지 않은 토큰입니다."));

		Card card = cardRepository.findByCardUniqueNumber(CardUniqueNumber.of(token.getCardUniqueNumber()))
			.orElseThrow(() -> new RuntimeException("카드 정보를 찾을 수 없습니다."));

		BankTransactionResponseDTO bankResult = bankAPI.createTransaction(bankTransactionCreateDTO.builder()
			.accountNumber(card.getAccountNumber())
			.amount(createTransactionRequest.getAmount())
			.userId(card.getUserCI())
			.type("TXN")
			.createdAt(createTransactionRequest.getCreatedAt())
			.build());



		if(bankResult.getApprovalCode().contains("TXN")){
			cardTransactionRepository.save(
				CardTransaction.builder()
					.cardUniqueNumber(card.getCardUniqueNumber())
					.accountNumber(card.getAccountNumber())
					.amount(createTransactionRequest.getAmount())
					.createdAt(bankResult.getCreatedAt())
					.merchantName(createTransactionRequest.getMerchantName())
					.approvalCode(bankResult.getApprovalCode())
					.build());
		}
		return bankResult;
	}

	@Transactional(readOnly = true)
	public List<CardTransactionEntity> getTransactionsByUserApiKeyAfterTimestamp(String userAPiKey,
		Timestamp timestamp) {
		Optional<User> optionalUser = userRepository.findByUserApiKey(userAPiKey);

		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			// 조회된 사용자 ID와 시작 시간으로 트랜잭션 조회
			return cardTransactionRepository.findByCard_UserCIAndCreatedAtAfterOrderByCreatedAtDesc(
				user.getUserCI(),
				timestamp
			);
		} else {
			throw new UserNotFoundException("User not found with API key: " + userAPiKey);
		}
	}

	@Transactional(readOnly = true)
	public List<CardTransactionEntity> getAllTransactionsByUserApiKey(String userAPiKey) {
		Optional<User> optionalUser = userRepository.findByUserApiKey(userAPiKey);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			Timestamp oldTimestamp = Timestamp.valueOf("1970-01-01 00:00:00");
			return cardTransactionRepository.findByCard_UserCIAndCreatedAtAfterOrderByCreatedAtDesc(
				user.getUserCI(),
				oldTimestamp
			);
		} else {
			throw new UserNotFoundException("User not found with API key: " + userAPiKey);
		}
	}
}