package com.kkulmoo.rebirth.common.webclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
@Configuration
public class WebClientConfig {

	// backend -> 카드사 (User의 카드 정보주세요)
	@Value("${api.first.base-url}")
	private String firstApiBaseUrl;

	@Value("${api.second.base-url}")
	private String secondApiBaseUrl;

	@Bean(name = "cardIssuerAPIClient")
	public WebClient cardIssuerAPIClient() {
		return WebClient.builder()
			.baseUrl(firstApiBaseUrl)
			.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
			.build();
	}

	@Bean(name = "ssafyBankAPIClient")
	public WebClient ssafyBankAPIClient() {
		return WebClient.builder()
			.baseUrl(secondApiBaseUrl)
			.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
			.build();
	}
}