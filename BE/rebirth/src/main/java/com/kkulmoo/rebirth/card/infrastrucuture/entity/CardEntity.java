package com.kkulmoo.rebirth.card.infrastrucuture.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kkulmoo.rebirth.card.domain.Card;
import com.kkulmoo.rebirth.card.domain.CardRepository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name = "cards")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CardEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "card_id")
	private Long cardId;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "card_template_id", nullable = false)
	private Long cardTemplateId;

	@Column(name = "card_unique_number", nullable = false)
	private String cardUniqueNumber;

	@Column(name = "expiry_date", nullable = false)
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

}

