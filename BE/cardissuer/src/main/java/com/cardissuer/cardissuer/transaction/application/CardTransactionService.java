package com.cardissuer.cardissuer.transaction.application;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cardissuer.cardissuer.cards.domain.CardRepository;
import com.cardissuer.cardissuer.common.exception.UserNotFoundException;
import com.cardissuer.cardissuer.transaction.domain.CardTransaction;
import com.cardissuer.cardissuer.transaction.domain.CardTransactionRepository;
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
	@Transactional
	public CardTransaction createTransaction(CreateTransactionRequest createTransactionRequest) {


		return cardTransactionRepository.save(CardTransaction.builder()
			.amount(createTransactionRequest.getAmount())
			.merchantName(createTransactionRequest.getMerchantName()).build());
	}

	@Transactional(readOnly = true)
	public List<CardTransactionEntity> getTransactionsByUserApiKeyAfterTimestamp(String userAPiKey, Timestamp timestamp) {
		Optional<User> optionalUser = userRepository.findByUserApiKey(userAPiKey);


		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			// 조회된 사용자 ID와 시작 시간으로 트랜잭션 조회
			return cardTransactionRepository.findByCard_UserIdAndCreatedAtAfterOrderByCreatedAtDesc(
				user.getUserId(),
				timestamp
			);
		} else {
			throw new UserNotFoundException("User not found with API key: " + userAPiKey);
		}
	}

	@Transactional(readOnly = true)
	public List<CardTransactionEntity> getAllTransactionsByUserApiKey(String userAPiKey) {
		Optional<User> optionalUser = userRepository.findByUserApiKey(userAPiKey);

		if(optionalUser.isPresent()){
			User user = optionalUser.get();

			Timestamp oldTimestamp = Timestamp.valueOf("1970-01-01 00:00:00");
			return cardTransactionRepository.findByCard_UserIdAndCreatedAtAfterOrderByCreatedAtDesc(
				user.getUserId(),
				oldTimestamp
			);
		}else {
			throw new UserNotFoundException("User not found with API key: " + userAPiKey);
		}
	}
}