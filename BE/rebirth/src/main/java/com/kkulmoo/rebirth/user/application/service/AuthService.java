package com.kkulmoo.rebirth.user.application.service;

import org.springframework.stereotype.Service;

import com.kkulmoo.rebirth.common.exception.UserCreationException;
import com.kkulmoo.rebirth.common.util.PasswordUtils;
import com.kkulmoo.rebirth.user.application.command.CreateUserCommand;
import com.kkulmoo.rebirth.user.domain.User;
import com.kkulmoo.rebirth.user.domain.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
	private final UserRepository userRepository;

	public User createUser(CreateUserCommand command) {
		// PIN 번호 암호화
		String hashedPinNumber = PasswordUtils.encodePassword(command.getPinNumber());

		// 사용자 객체 생성
		User newUser = User.builder()
			.userName(command.getUserName())
			.hashedPinNumber(hashedPinNumber)
			.phoneNumber(command.getPhoneNumber())
			.phoneSerialNumber(command.getPhoneSerialNumber())
			.build();

		// 사용자 저장
		User createdUser  = userRepository.save(newUser);

		if (createdUser == null) {
			throw new UserCreationException("사용자 생성에 실패했습니다.");
		}

		return createdUser;
	}

}
