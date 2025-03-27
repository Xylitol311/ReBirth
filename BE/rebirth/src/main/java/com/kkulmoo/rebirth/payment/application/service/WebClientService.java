package com.kkulmoo.rebirth.payment.application.service;

import com.kkulmoo.rebirth.payment.presentation.ConversionUtils;
import com.kkulmoo.rebirth.payment.presentation.request.CreateTransactionRequestDTO;
import com.kkulmoo.rebirth.payment.presentation.response.CardTransactionDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class WebClientService {
    private final WebClient webClient;

    public WebClientService(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<CardTransactionDTO> checkPermanentToken(CreateTransactionRequestDTO createTransactionRequestDTO) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/cards/test")
                        .queryParams(ConversionUtils.toQueryParams(createTransactionRequestDTO))
                        .build())
                .retrieve()
                .bodyToMono(CardTransactionDTO.class); // JSON을 CardTransactionDTO로 변환
    }


}