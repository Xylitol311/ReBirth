package com.kkulmoo.rebirth.user.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kkulmoo.rebirth.common.APIResponseDTO.ApiResponseDTO;
import com.kkulmoo.rebirth.common.annotation.UserId;
import com.kkulmoo.rebirth.user.application.command.CreateUserCommand;
import com.kkulmoo.rebirth.user.application.service.AuthService;
import com.kkulmoo.rebirth.user.presentation.requestDTO.UserSignupRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/signup")
	public ResponseEntity<ApiResponseDTO<Void>> signup(
		@UserId UserId userId,
		@RequestBody UserSignupRequest request) {
		authService.createUser(CreateUserCommand.fromRequest(request));

		return ResponseEntity.status(HttpStatus.CREATED)
			.body(ApiResponseDTO.success("회원가입이 성공적으로 완료되었습니다."));
	}


}
