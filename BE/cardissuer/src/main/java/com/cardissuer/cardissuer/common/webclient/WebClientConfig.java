package com.cardissuer.cardissuer.common.webclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
@Configuration
public class WebClientConfig {

	// 카드사 -> rebirth
	@Value("${api.rebirth.base-url}")
	private String rebirthUrl;

	//
	@Value("${api.bank.base-url}")
	private String bankUrl;

	@Bean(name = "rebirthAPIClient")
	public WebClient rebirthAPIClient() {
		return WebClient.builder()
			.baseUrl(rebirthUrl)
			.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
			.build();
	}

	@Bean(name = "bankAPIClient")
	public WebClient bankAPIClient() {
		return WebClient.builder()
			.baseUrl(bankUrl)
			.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
			.build();
	}
}