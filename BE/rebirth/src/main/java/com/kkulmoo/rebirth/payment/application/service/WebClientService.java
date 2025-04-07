package com.kkulmoo.rebirth.payment.application.service;

import com.kkulmoo.rebirth.payment.presentation.request.CreateTransactionRequestToCardsaDTO;
import com.kkulmoo.rebirth.payment.presentation.request.PermanentTokenRequestToCardsaDTO;
import com.kkulmoo.rebirth.payment.presentation.response.CardTransactionDTO;
import com.kkulmoo.rebirth.payment.presentation.response.PermanentTokenResponseByCardsaDTO;
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

    public Mono<CardTransactionDTO> checkPermanentToken(CreateTransactionRequestToCardsaDTO createTransactionRequestToCardsaDTO) {
        return webClient.post()
                .uri("/api/transactions")
                .bodyValue(createTransactionRequestToCardsaDTO)
                .retrieve()
                .bodyToMono(CardTransactionDTO.class);
    }

    public Mono<PermanentTokenResponseByCardsaDTO> createCard(PermanentTokenRequestToCardsaDTO permanentTokenRequest) {
        return webClient.post()
                .uri("/api/cards/tokens")
                .bodyValue(permanentTokenRequest)
                .retrieve()
                .bodyToMono(PermanentTokenResponseByCardsaDTO.class);
    }



}