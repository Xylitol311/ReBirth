package com.kkulmoo.rebirth.user.application.command;

import com.kkulmoo.rebirth.user.presentation.requestDTO.UserSignupRequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class CreateUserCommand {
	private final String userName;
	private final String pinNumber;
	private final String phoneNumber;
	private final String phoneSerialNumber;
	private final String birth;
	private final String userCI;

	// DTO에서 Command로 변환하는 팩토리 메서드
	public static CreateUserCommand fromRequest(UserSignupRequest request, String userCI) {
		return CreateUserCommand.builder()
			.userName(request.getUserName())
			.pinNumber(request.getPinNumber())
			.phoneNumber(request.getPhoneNumber()).birth(request.getBirth())
			.phoneSerialNumber(request.getDeviceId())
				.userCI(userCI)
			.build();
	}
}