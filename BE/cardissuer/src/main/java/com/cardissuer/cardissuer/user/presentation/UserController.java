package com.cardissuer.cardissuer.user.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cardissuer.cardissuer.user.application.UserService;
import com.cardissuer.cardissuer.user.domain.User;
import com.cardissuer.cardissuer.user.presentation.requestdto.UserRegistrationDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	/**
	 * 새로운 사용자를 등록합니다.
	 *
	 * @return ResponseEntity 객체
	 */
	@PostMapping
	public ResponseEntity<User> registerUser(@RequestBody UserRegistrationDto registrationDto) {
		System.out.println(registrationDto.toString());
		User createdUser = userService.createUser(registrationDto);
		return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
	}
}
