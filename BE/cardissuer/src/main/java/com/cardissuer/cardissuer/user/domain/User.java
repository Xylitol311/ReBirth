package com.cardissuer.cardissuer.user.domain;

import java.time.LocalDateTime;

import jakarta.persistence.GeneratedValue;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class User {
	Integer userId;
	String userName;
	String userApiKey;
    LocalDateTime createdAt; // 선택적으로 추가

}
