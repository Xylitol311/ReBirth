package com.kkulmoo.rebirth.user.presentation;

import com.kkulmoo.rebirth.common.ApiResponseDTO.ApiResponseDTO;
import com.kkulmoo.rebirth.common.annotation.JwtUserId;
import com.kkulmoo.rebirth.user.application.command.CreateUserCommand;
import com.kkulmoo.rebirth.user.application.service.AuthService;
import com.kkulmoo.rebirth.user.domain.UserId;
import com.kkulmoo.rebirth.user.presentation.requestDTO.UserCIRequest;
import com.kkulmoo.rebirth.user.presentation.requestDTO.UserLoginRequest;
import com.kkulmoo.rebirth.user.presentation.requestDTO.UserSignupRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	// 회원가입 1차단계
	@PostMapping("/signup")
	public ResponseEntity<ApiResponseDTO<Void>> signup(
		@RequestBody UserSignupRequest request) {

//		//은행 한테 사용자 이름하고 birth 넘겨주면서 user CI 받아오기
//		String userCI = authService.getUserCI(UserCIRequest.builder().userName(request.getUserName()).birth(request.getBirth()).build());
		String userCI= "test";
		authService.createUser(CreateUserCommand.fromRequest(request,userCI));

	//jwt 토큰도 헤더에 같이 넘겨주기
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(ApiResponseDTO.success("회원가입이 성공적으로 완료되었습니다."));
	}

	//회원가입 2차단계
	//카드 정보, 은행 정보 싸그리 싹싹 가져와서 DB 업데이트 시키기 ( 나중에 ...)
	// -> UserController로 쓰면 되는건가?

	// 회원가입 3차단계
	// 패턴 로그인 선택시 ( 패턴 로그인 저장 ) + 암호화
	@PostMapping("/registpattern")
	public ResponseEntity<ApiResponseDTO<Void>> registPattern(
			@JwtUserId Integer userId,
			@RequestBody String patternNumbers) {

		//패턴 ID를 업데이트 하기
		authService.createPatternNum(userId,patternNumbers);

		return ResponseEntity.status(HttpStatus.OK)
				.body(ApiResponseDTO.success("패턴등록이 성공적으로 완료되었습니다."));

	}

	//로그인 할 경우 - 패턴/핀/지문에 따라 다르게 처리)
	//패턴/PIN일 경우 까서 확인하기, 지문일 경우 jwt 토큰만 넘겨주면 됨
	@PostMapping("/login")
	public ResponseEntity<ApiResponseDTO<Void>> login(
			@RequestBody UserLoginRequest userLoginRequest
	)
	{
		if(userLoginRequest.getType().equals("fingerprint")){

			//jwt 토큰 넘겨주는 로직
			return ResponseEntity.status(HttpStatus.OK)
					.body(ApiResponseDTO.success("로그인이 완료되었습니다."));
		}

		if(authService.validUser(userLoginRequest.getNumber(), userLoginRequest.getType(), userLoginRequest.getPhoneSerialNumber()))
			return ResponseEntity.status(HttpStatus.OK)
					.body(ApiResponseDTO.success("로그인이 완료 되었습니다."));

		else return ResponseEntity.status(HttpStatus.OK)
			.body(ApiResponseDTO.error("로그인 번호가 틀렸습니다"));

	}


}
