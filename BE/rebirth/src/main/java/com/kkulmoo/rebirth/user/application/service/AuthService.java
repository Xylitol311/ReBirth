package com.kkulmoo.rebirth.user.application.service;


import com.kkulmoo.rebirth.user.domain.UserId;
import com.kkulmoo.rebirth.user.presentation.requestDTO.UserCIRequest;
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
	private final UserWebClientService webClientService;

	public User createUser(CreateUserCommand command) {
		// PIN 번호 암호화
		String hashedPinNumber = PasswordUtils.encodePassword(command.getPinNumber());

		// 사용자 객체 생성
		User newUser = User.builder()
			.userName(command.getUserName())
				.userCI(command.getUserCI())
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

	public void createPatternNum(int userId, String patternNumbers){

		// Pattern 번호 암호화
		String hashedPatternNumber = PasswordUtils.encodePassword(patternNumbers);
		User user = userRepository.findByUserId(new UserId(userId));

		userRepository.update(User.builder().build());

	}

	public String getUserCI(UserCIRequest userCIRequest){

		return webClientService.getUserCI(userCIRequest).block();
	}

	public Boolean validUser(String number, String type, String phoneSerialNumber){

		//핀 일 경우
		if(type.equals("pin")){
			String hashedPinNumber = PasswordUtils.encodePassword(number);
			User user = userRepository.findByPhoneSerialNumber(phoneSerialNumber);

			if(user.getHashedPinNumber().equals(hashedPinNumber))
				return true;
			return false;
		}else if(type.equals("pattern")){
			String hashedPatternNumber = PasswordUtils.encodePassword(number);
			User user = userRepository.findByPhoneSerialNumber(phoneSerialNumber);

			if(user.getHashedPinNumber().equals(hashedPatternNumber))
				return true;
			return false;

		}else return false;
	}

}
