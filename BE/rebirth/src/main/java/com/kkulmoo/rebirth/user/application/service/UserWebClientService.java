package com.kkulmoo.rebirth.user.application.service;

import com.kkulmoo.rebirth.user.presentation.requestDTO.UserCIRequest;
import com.kkulmoo.rebirth.user.presentation.responseDTO.UserCIDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class UserWebClientService {
    private final WebClient webClient;

    public UserWebClientService(@Qualifier("ssafyBankAPIClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<String> getUserCI(UserCIRequest userCIRequest) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/users/userci")
                        .queryParam("userName", userCIRequest.getUserName())
                        .queryParam("birth", userCIRequest.getBirth())
                        .build())
                .retrieve()
                .bodyToMono(UserCIDTO.class)
                .map(UserCIDTO::getUserCI); // userCI 값만 추출
    }
}
