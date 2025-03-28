package com.kkulmo.bank.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

	@Value("${api.cardissuer.base-url}")
	private String baseUrl;

	@Bean
	public WebClient apiClient() {
		return WebClient.builder()
			.baseUrl(baseUrl)
			.defaultHeader("Content-Type", "application/json")
			.build();
	}
}