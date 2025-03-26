package com.cardissuer.cardissuer.transaction.infrastrucuture;


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

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "card_unique_number", referencedColumnName = "card_unique_number", insertable = false, updatable = false)
	private CardEntity card;

	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
	}
}
