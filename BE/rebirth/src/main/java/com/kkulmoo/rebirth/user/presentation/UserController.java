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
	private final MyDataService myDataService;

	// 은행 계좌 거래내역 불러오기 bankTransaction
	@PostMapping("/mydata/bank/transactions")
	public ResponseEntity<ApiResponseDTO<Void>> loadBankTransactions(@JwtUserId Integer userId){
//		myDataService.loadMyBankTransaction(userId);
		return ResponseEntity.ok(ApiResponseDTO.success("계좌 거래내역 로드에 성공하였습니다."));
	}

	// 카드 거래내역 불러오기 getCardTransaction
	@PostMapping("/mydata/card/transactions")
	public ResponseEntity<ApiResponseDTO<Void>> loadCardTransactions(@JwtUserId Integer userId){
//		myDataService.getMyCardTransactionData(userId);
		return ResponseEntity.ok(ApiResponseDTO.success("카드 거래내역 로드에 성공하였습니다."));
	}

	// 카드 자산 불러오기(MyCard 갱신)
	@PostMapping("/mydata/mycard")
	public ResponseEntity<ApiResponseDTO<Void>> loadMyCard(@JwtUserId Integer userId){
		 //todo: 이름 바꾸기
//		  myDataService.loadMyCard(2);
		 return ResponseEntity.ok(ApiResponseDTO.success("카드 로드에 성공하였습니다."));
	}

	// 은행 자산 불러오기(account 갱신)
	@PostMapping("/mydata/bank")
	public ResponseEntity<ApiResponseDTO<Void>> loadMyBank(@JwtUserId Integer userId){
//		myDataService.loadMyBankAccount(userId);
		return ResponseEntity.ok(ApiResponseDTO.success("은행 계좌 로드에 성공하였습니다."));
	}


	// todo: UserId를 Integer에서 UserId로 바꿔야함
	@DeleteMapping
	public ResponseEntity<ApiResponseDTO<Void>> deleteUser(@JwtUserId Integer userId) {
		userService.deleteUser(userId);
		return ResponseEntity.ok(ApiResponseDTO.success("삭제에 성공하였습니다."));
	}
}

