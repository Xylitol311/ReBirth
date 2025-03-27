package com.cardissuer.cardissuer.user.presentation.requestdto;

import java.sql.Timestamp;

import lombok.Getter;

@Getter
public class UserRegistrationDto {
	String userName;
	String userCI;
	Timestamp createdAt;
}
