package com.kkulmo.bank.user.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CardIssuerRequest {
	String userName;
	String userCI;
	LocalDateTime createdAt;
}

