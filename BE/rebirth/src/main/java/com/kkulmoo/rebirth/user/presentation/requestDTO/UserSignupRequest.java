package com.kkulmoo.rebirth.user.presentation.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserSignupRequest {
	String userName;
	String userEmail;
	String pinNumber;
	String phoneNumber;
	String deviceId;

}