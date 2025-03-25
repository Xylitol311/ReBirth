package com.kkulmoo.rebirth.user.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kkulmoo.rebirth.common.APIResponseDTO.ApiResponseDTO;
import com.kkulmoo.rebirth.common.annotation.UserId;
import com.kkulmoo.rebirth.user.application.service.UserService;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;


	// @PostMapping
	// public ResponseEntity<ApiResponseDTO<Void>> setMyData(@UserId Integer userId){
	//
	// }

	@DeleteMapping
	public ResponseEntity<ApiResponseDTO<Void>> deleteUser(@UserId Integer userId) {
		userService.deleteUser(userId);
		return ResponseEntity.ok(ApiResponseDTO.success("삭제에 성공하였습니다."));
	}


}
