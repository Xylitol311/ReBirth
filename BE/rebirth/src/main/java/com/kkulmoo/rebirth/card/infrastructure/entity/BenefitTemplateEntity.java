package com.kkulmoo.rebirth.card.infrastructure.entity;

import com.kkulmoo.rebirth.card.domain.BenefitType;
import com.kkulmoo.rebirth.card.domain.DiscountType;
import com.kkulmoo.rebirth.shared.entity.CardTemplateEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

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

	@Column(name = "category_id", columnDefinition = "integer[]")
	private List<Integer> categoryIds;

	@Column(name = "subcategory_id", columnDefinition = "integer[]")
	private List<Integer> subcategoryIds;

	@Column(name = "benefit_type", nullable = false)
	@Enumerated(EnumType.STRING)
	private BenefitType benefitType;

	@Column(name = "benefit_method", nullable = false)
	private Short benefitMethod;

	@Column(name = "performance_range", nullable = false)
	private Integer[] performanceRange;

	@Column(name = "performance_range_by_benefit")
	private Double[] performanceRangeByBenefit;

	@Column(name = "merchant_info", nullable = false)
	private Boolean merchantInfo;

	@Column(name = "merchant_list")
	private String[] merchantList;

	@Column(name = "payment_range")
	private Integer[] paymentRange;

	@Column(name = "benefit_usage_limit")
	private Short[] benefitUsageLimit;

	@Column(name = "benefit_usage_amount")
	private Short[] benefitUsageAmount;

	@Column(name = "discount_type")
	@Enumerated(EnumType.STRING)
	private DiscountType discountType;

	@Column(name = "additional_info")
	private String additionalInfo;
}