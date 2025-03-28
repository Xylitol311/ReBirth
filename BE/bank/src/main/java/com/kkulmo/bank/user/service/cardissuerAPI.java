package com.kkulmo.bank.user.service;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.kkulmo.bank.user.dto.CardIssuerRequest;
import com.kkulmo.bank.user.dto.cardIssuerUserResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class cardissuerAPI {

	private final WebClient webClient;

	public cardIssuerUserResponse createCardIssuerUser(CardIssuerRequest request) {
		return webClient.post()
			.uri("/users") // 엔드포인트 지정
			.bodyValue(request) // 요청 본문 설정
			.retrieve() // 응답 검색 시작
			.bodyToMono(cardIssuerUserResponse.class) // 응답 본문을 CardIssueResponse 클래스로 변환
			.block(); // 동기식으로 응답 대기 (필요한 경우)
	}
}
