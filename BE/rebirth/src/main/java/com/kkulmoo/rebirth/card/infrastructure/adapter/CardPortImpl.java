package com.kkulmoo.rebirth.card.infrastructure.adapter;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.kkulmoo.rebirth.card.application.CardPort;
import com.kkulmoo.rebirth.card.infrastructure.adapter.dto.CardApiResponse;
import com.kkulmoo.rebirth.common.exception.CardFetchException;
import com.kkulmoo.rebirth.user.domain.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class CardPortImpl implements CardPort {

	@Qualifier("cardIssuerAPIClient") // 또는 secondApiClient 중 적절한 것 선택
	private final WebClient cardIssuerAPIClient;

	@Override
	public List<CardApiResponse> fetchCardData(User user) {
		try {
			// WebClient로 API 호출 후 바로 List<CardApiResponse>로 반환
			List<CardApiResponse> cardList = cardIssuerAPIClient.get()
				.uri("/cards")
				.header("Authorization", "Bearer " + user.getUserApiKey())
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<List<CardApiResponse>>() {})
				.block();

			log.info("사용자 {}의 카드 {}개를 가져왔습니다", user.getUserName(),
				cardList != null ? cardList.size() : 0);

			return cardList != null ? cardList : Collections.emptyList();
		} catch (Exception e) {
			log.error("사용자 {}의 카드 데이터 가져오기 실패: {}", user.getUserName(), e.getMessage());
			throw new CardFetchException("카드 데이터를 가져오는데 실패했습니다", e);
		}
	}
}