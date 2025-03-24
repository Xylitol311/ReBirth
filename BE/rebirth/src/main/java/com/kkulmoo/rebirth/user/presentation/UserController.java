package com.kkulmoo.rebirth.user.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.kkulmoo.rebirth.common.ApiResponseDTO;
import com.kkulmoo.rebirth.common.annotation.UserId;
import com.kkulmoo.rebirth.user.application.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;


	@DeleteMapping
	public ResponseEntity<ApiResponseDTO<Void>> deleteUser(@UserId Integer userId) {
		userService.deleteUser(userId);
		return ResponseEntity.ok(ApiResponseDTO.success("삭제에 성공하였습니다."));
	}
}
