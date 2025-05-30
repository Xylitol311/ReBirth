package com.kkulmoo.rebirth.shared.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cards")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor // 이 부분 추가
public class CardEntity {
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

	@Column(name = "account_number")
	private String accountNumber;

	@Column(name = "card_order", nullable = false)
	private Short cardOrder;

	@Column(name = "annual_fee", nullable = false)
	private Integer annualFee;

	@Column(name = "permanent_token")
	private String permanentToken;

	@Column(name = "payment_card_order")
	private Short paymentCardOrder;

	@Column(name = "spending_tier")
	private Short spendingTier;

	@Column(name = "card_name")
	private String cardName;

	@Column(name = "expiry_date")
	private LocalDate expiryDate;

	@Column(name = "is_expired", nullable = false)
	private Short isExpired;

	@Column(name = "created_at", nullable = false)
	@CreationTimestamp
	private LocalDateTime createdAt;

	@Column(name = "payment_created_at")
	private LocalDateTime paymentCreatedAt;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@Column(name = "latest_load_data_at")
	private LocalDateTime latestLoadDataAt;
}

