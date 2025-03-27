package com.kkulmo.bank.transactions.repository;

import java.time.LocalDateTime;

import com.kkulmo.bank.transactions.dto.TransactionType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "transaction")
@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TransactionEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;

	@Column(name = "account_number")
	private String accountNumber;

	@Column(name = "amount")
	private Long amount;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "type")
	@Enumerated(EnumType.STRING)  // Added for ENUM type
	private TransactionType type;  // 입금,이체,체크카드

	@Column(name = "description")
	private String description;

	@Column(name = "approval_code", nullable = false) // 추가된 필드
	private String approvalCode;

}