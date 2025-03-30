package com.kkulmoo.rebirth.common.ApiResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponseDTO<T> {
	private boolean success;
	private String message;
	private T data;

	// 성공 응답 생성 메서드 (데이터 포함)
	public static <T> ApiResponseDTO<T> success(String message, T data) {
		return ApiResponseDTO.<T>builder()
			.success(true)
			.message(message)
			.data(data)
			.build();
	}

	// 성공 응답 생성 메서드 (데이터 미포함)
	public static <T> ApiResponseDTO<T> success(String message) {
		return ApiResponseDTO.<T>builder()
			.success(true)
			.message(message)
			.data(null)
			.build();
	}

	// 실패 응답 생성 메서드
	public static <T> ApiResponseDTO<T> error(String message) {
		return ApiResponseDTO.<T>builder()
			.success(false)
			.message(message)
			.data(null)
			.build();
	}
}