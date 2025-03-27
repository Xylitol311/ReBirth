package com.kkulmo.bank.user.dto;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class UserDTO {
	private String userId;
	private String name;
	private String monthdaybirth; // 생년월일 필드 추가
	private LocalDateTime createdAt;
}
