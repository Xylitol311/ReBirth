package com.cardissuer.cardissuer.user.application;

import org.springframework.stereotype.Service;

import com.cardissuer.cardissuer.user.domain.User;
import com.cardissuer.cardissuer.user.domain.UserRepository;
import com.cardissuer.cardissuer.user.presentation.requestdto.UserRegistrationDto;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;

	@Transactional
	public User createUser(UserRegistrationDto registrationDto) {
		return userRepository.save(User.builder()
			.userName(registrationDto.getUserName())
			.userCI(registrationDto.getUserCI())
			.createdAt(registrationDto.getCreatedAt())
			.build());
	}

}