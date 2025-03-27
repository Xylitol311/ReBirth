package com.kkulmo.bank.user.dto;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
	@Id
	@Column(name = "user_ci")
	private String userId;

	@Column(name = "name")
	private String name;

	@Column(name = "birth" , length = 6)
	private String monthdaybirth;

	@Column(name = "created_at")
	private LocalDateTime createdAt;
}
