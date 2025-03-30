package com.kkulmo.bank.account.repository;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "account")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountEntity {

	@Id
	@Column(name = "account_number")
	private String accountNumber;

	@Column(name = "user_id")
	private String userId;

	@Column(name = "balance")
	private Long balance;

	@Column(name = "created_at" , nullable = false)
	private LocalDateTime createdAt;


	// Helper method to update balance
	public void updateBalance(Long amount) {
		this.balance = this.balance + amount;
	}
}