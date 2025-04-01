package com.kkulmoo.rebirth.shared.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cards")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor // 이 부분 추가
public class CardsEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "card_id")
	private Integer cardId;

	@Column(name = "user_id", nullable = false)
	private Integer userId;

	@Column(name = "card_template_id", nullable = false)
	private Integer cardTemplateId;

	@Column(name = "card_unique_number", nullable = false)
	private String cardUniqueNumber;

	@Column(name = "card_number")
	private String cardNumber;

	@Column(name = "expiry_date")
	private LocalDate expiryDate;

	@Column(name = "card_order", nullable = false)
	private Short cardOrder;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@Column(name = "is_expried", nullable = false)
	private Short isExpired;

	@Column(name = "annual_fee", nullable = false)
	private Integer annualFee;

	@Column(name = "permanent_token")
	private String permanentToken;

	@Column(name = "payment_card_order")
	private Short paymentCardOrder;

	@Column(name = "payment_created_at")
	private LocalDateTime paymentCreatedAt;

	@Column(name = "spending_tier")
	private Short spendingTier;
}

