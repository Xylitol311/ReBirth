package com.kkulmoo.rebirth.card.infrastrucuture.entity;

import com.kkulmoo.rebirth.transactions.infrastructure.CategoryEntity;
import com.kkulmoo.rebirth.transactions.infrastructure.MerchantEntity;
import com.kkulmoo.rebirth.transactions.infrastructure.SubcategoryEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "benefit_templates")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class BenefitTemplateEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "benefit_template_id")
	private Integer benefitTemplateId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "card_template_id", nullable = false)
	private CardTemplateEntity cardTemplate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "merchant_id")
	private MerchantEntity merchant;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id", nullable = false)
	private CategoryEntity category;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "subcategory_id", nullable = false)
	private SubcategoryEntity subcategory;

	@Column(name = "max_benefit_limit")
	private Integer maxBenefitLimit;

	@Column(name = "max_benefit_count_limit_month")
	private Short maxBenefitCountLimitMonth;

	@Column(name = "max_benefit_count_limit_year")
	private Short maxBenefitCountLimitYear;

	@Column(name = "benefit_type", nullable = false)
	private String benefitType;

	@Column(name = "benefit_amount")
	private Integer benefitAmount;

	@Column(name = "spending_tier")
	private Short spendingTier;

	@Column(name = "spending_min_amount")
	private Integer spendingMinAmount;

	@Column(name = "spending_max_amount")
	private Integer spendingMaxAmount;

	@Column(name = "coverage_type", nullable = false)
	private String coverageType;
}