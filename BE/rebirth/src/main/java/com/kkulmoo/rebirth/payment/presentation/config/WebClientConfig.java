package com.kkulmoo.rebirth.payment.presentation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.baseUrl("http://localhost:8081") // 서버 모듈의 기본 URL
                .defaultHeader("Content-Type", "application/json") // 기본 헤더 설정
                .build();
    }
}

