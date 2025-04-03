package com.kkulmoo.rebirth.user.application.service;

import com.kkulmoo.rebirth.common.ApiResponseDTO.ApiResponseDTO;
import com.kkulmoo.rebirth.payment.presentation.request.CreateTransactionRequestDTO;
import com.kkulmoo.rebirth.payment.presentation.response.CardTransactionDTO;
import com.kkulmoo.rebirth.user.presentation.requestDTO.UserCIRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class WebClientService {
    private final WebClient webClient;

    public WebClientService(@Qualifier("ssafyBankAPIClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<String> getUserCI(UserCIRequest userCIRequest) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/users/userci")
                        .queryParam("userName", userCIRequest.getUserName()) // 요청 객체의 필드 활용
                        .queryParam("birth", userCIRequest.getBirth()) // 필요한 추가 필드들
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponseDTO<String>>() {})
                .map(ApiResponseDTO::getData); // 응답 객체에서 실제 CI 값만 추출
    }
}
