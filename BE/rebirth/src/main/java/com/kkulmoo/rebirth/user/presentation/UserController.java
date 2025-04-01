package com.kkulmoo.rebirth.user.presentation;

import com.kkulmoo.rebirth.common.ApiResponseDTO.ApiResponseDTO;
import com.kkulmoo.rebirth.common.annotation.JwtUserId;
import com.kkulmoo.rebirth.user.application.service.MyDataService;
import com.kkulmoo.rebirth.user.application.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;
	private final MyDataService myDataService;;


	// todo: UserId를 Integer에서 UserId로 바꿔야함
	 @PostMapping
	 public ResponseEntity<ApiResponseDTO<Void>> setMyData(@JwtUserId Integer userId){
		 myDataService.getMyTransactionData(userId);
		 return ResponseEntity.ok(ApiResponseDTO.success("마이데이터 연동이 성공적으로 완료되었습니다."));
	 }


	// todo: UserId를 Integer에서 UserId로 바꿔야함
	@DeleteMapping
	public ResponseEntity<ApiResponseDTO<Void>> deleteUser(@JwtUserId Integer userId) {
		userService.deleteUser(userId);
		return ResponseEntity.ok(ApiResponseDTO.success("삭제에 성공하였습니다."));
	}

}

