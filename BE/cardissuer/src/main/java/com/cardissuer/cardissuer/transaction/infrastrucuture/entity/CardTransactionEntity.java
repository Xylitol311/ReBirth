package com.cardissuer.cardissuer.transaction.infrastrucuture.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import com.cardissuer.cardissuer.cards.infrastructure.CardEntity;


@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Table(name = "card_transaction")
public class CardTransactionEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "transaction_id")
	private Integer transactionId;

	@Column(name = "card_unique_number", nullable = false)
	private String cardUniqueNumber;

	@Column(name = "amount")
	private Integer amount;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "merchant_name", length = 50, nullable = false)
	private String merchantName;

	@Column(name = "benefit_amount")
	private Integer benefitAmount;

	@Column(name = "benefit_type", length = 20)
	private String benefitType;

	@Column(name = "approval_code", length = 50)
	private String approvalCode;

	@Column(name = "benefit_id")
	private Integer benefitId;
}