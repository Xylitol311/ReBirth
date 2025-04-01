package com.kkulmoo.rebirth.card.application;

import com.kkulmoo.rebirth.card.infrastructure.adapter.dto.CardApiResponse;
import com.kkulmoo.rebirth.transactions.application.dto.CardTransactionRequest;
import com.kkulmoo.rebirth.transactions.application.dto.CardTransactionResponse;
import com.kkulmoo.rebirth.user.domain.User;
import reactor.core.publisher.Mono;

import java.util.List;

public interface CardPort {
	List<CardApiResponse> fetchCardData(User user);
	Mono<List<CardTransactionResponse>> getCardTransaction(CardTransactionRequest cardTransactionRequest);
}