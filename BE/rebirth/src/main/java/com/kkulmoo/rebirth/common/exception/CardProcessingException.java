package com.kkulmoo.rebirth.common.exception;


public class CardProcessingException extends RuntimeException {

	// 메시지만 받는 생성자
	public CardProcessingException(String message) {
		super(message);
	}

	// 메시지와 원인 예외를 받는 생성자
	public CardProcessingException(String message, Throwable cause) {
		super(message, cause);
	}


}