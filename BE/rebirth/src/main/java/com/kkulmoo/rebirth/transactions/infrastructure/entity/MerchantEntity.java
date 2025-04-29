package com.kkulmoo.rebirth.transactions.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "merchants")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MerchantEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "merchant_id")
	private Integer merchantId;

	// 외래키 관계
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "subcategory_id", nullable = false)
	private SubcategoryEntity subcategory;

	@Column(name = "merchant_name", length = 100)
	private String merchantName;

	@Column(name = "brand_name", length = 100)
	private String brandName;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

}
