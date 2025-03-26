package com.cardissuer.cardissuer.cards.presentation;


import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cardissuer.cardissuer.cards.application.CardService;
import com.cardissuer.cardissuer.cards.application.CardResponse;
import com.cardissuer.cardissuer.cards.domain.PermanentToken;
import com.cardissuer.cardissuer.transaction.presentation.PermanentTokenRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

	private final CardService cardService;

	@PostMapping("/tokens")
	public ResponseEntity<?> getPermanentToken(
		@RequestHeader("Authorization") String authorizationHeader,
		@RequestBody PermanentTokenRequest permanentTokenRequest) {
		// Authorization을 받아서
		String ssafyAPIKey = authorizationHeader.replace("Bearer ", "");
		PermanentToken token = cardService.getPermanentToken(ssafyAPIKey, permanentTokenRequest);
		return ResponseEntity.ok(token);
	}

	@GetMapping
	public ResponseEntity<List<CardResponse>> getUserCards(
		@RequestHeader("Authorization") String authorizationHeader)
	{
		String userAPI = authorizationHeader.replace("Bearer ", "");
		// 사용자의 모든 카드 목록 조회
		List<CardResponse> cardDetail = cardService.getCardsByUserApiKey(userAPI);
		return ResponseEntity.ok(cardDetail);
	}

}
