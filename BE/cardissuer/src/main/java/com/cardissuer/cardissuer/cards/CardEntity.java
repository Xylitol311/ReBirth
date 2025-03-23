package com.cardissuer.cardissuer.cards;

import java.sql.Timestamp;
import java.util.Date;

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
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "card_unique_number")
	private Long cardUniqueNumber;

	@Column(name = "user_id")
	private Integer userId;

	@Column(name = "card_number")
	private String cardNumber;

	@Column(name = "expiry_date")
	private Date expiryDate;

	@Column(name = "cvc")
	private String cvc;

	@Column(name = "created_at")
	private Timestamp createdAt;

	@Column(name = "deleted_at")
	private Timestamp deletedAt;

	@Column(name = "annual_fee")
	private Integer annualFee;
}