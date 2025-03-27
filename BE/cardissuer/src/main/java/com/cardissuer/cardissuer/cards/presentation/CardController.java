package com.cardissuer.cardissuer.cards.presentation;


import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cardissuer.cardissuer.cards.application.CardService;
import com.cardissuer.cardissuer.cards.application.CardResponse;
import com.cardissuer.cardissuer.cards.domain.Card;
import com.cardissuer.cardissuer.cards.domain.PermanentToken;
import com.cardissuer.cardissuer.cards.infrastructure.CardEntity;
import com.cardissuer.cardissuer.transaction.presentation.PermanentTokenRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

	private final CardService cardService;

	@PostMapping("/tokens")
	public ResponseEntity<?> getPermanentToken(
		@RequestBody PermanentTokenRequest permanentTokenRequest) {
		// Authorization을 받아서
		String userCI = permanentTokenRequest.getUserCI();
		System.out.println(userCI);
		PermanentToken token = cardService.getPermanentToken(userCI, permanentTokenRequest);
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

	@PostMapping
	public ResponseEntity<?> createCard(@RequestBody CardCreateRequest request) {
		try {

			// 모든 정보를 cardService에 넘겨서 처리
			Card savedCard = cardService.createCard(request);
			// 성공 응답 반환
			return ResponseEntity.status(HttpStatus.CREATED).body("카드가 성공적으로 생성되었습니다");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("카드 생성 중 오류 발생: " + e.getMessage());
		}
	}
}
