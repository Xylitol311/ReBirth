package com.cardissuer.cardissuer.cards.infrastructure;

import java.sql.Timestamp;
import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cards")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CardEntity {

	@Id
	@Column(name = "card_unique_number", length = 36)
	private String cardUniqueNumber;  // UUID를 String으로 처리

	@Column(name = "user_ci", nullable = false)
	private String userCI;

	@Column(name = "account_number" , nullable = false)
	private String accountNumber;

	@Column(name = "card_number")
	private String cardNumber;

	@Column(name = "card_name")
	private String cardName;

	@Column(name = "expiry_date")
	private String expiryDate;

	@Column(name = "cvc")
	private String cvc;

	@Column(name = "card_password")
	private String cardPassword;

	@Column(name = "created_at", nullable = false, updatable = false)
	@CreationTimestamp
	private Timestamp createdAt;

	@Column(name = "deleted_at")
	private Timestamp deletedAt;
}