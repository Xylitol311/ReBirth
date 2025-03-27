package com.kkulmoo.rebirth.payment.application.service;

import com.kkulmoo.rebirth.payment.presentation.request.CreateTransactionRequestDTO;
import com.kkulmoo.rebirth.payment.presentation.response.CardTransactionDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class WebClientService {
    private final WebClient webClient;

    public WebClientService(@Qualifier("cardIssuerAPIClient")WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<CardTransactionDTO> checkPermanentToken(CreateTransactionRequestDTO createTransactionRequestDTO) {
        return webClient.post()
                .uri("/api/transactions")
                .bodyValue(createTransactionRequestDTO)
                .retrieve()
                .bodyToMono(CardTransactionDTO.class);
    }


}