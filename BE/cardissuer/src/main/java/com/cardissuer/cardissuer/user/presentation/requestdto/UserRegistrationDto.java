package com.cardissuer.cardissuer.user.presentation.requestdto;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class UserRegistrationDto {
	String userName;
	String userCI;
	LocalDateTime createdAt;
}
