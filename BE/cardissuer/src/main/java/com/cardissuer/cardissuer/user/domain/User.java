package com.cardissuer.cardissuer.user.domain;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import jakarta.persistence.GeneratedValue;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class User {
	String userCI;
	String userName;
	Timestamp createdAt;
	Timestamp deletedAt;
}
