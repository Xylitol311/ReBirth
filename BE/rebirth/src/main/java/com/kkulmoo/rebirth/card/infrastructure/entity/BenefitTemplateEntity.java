package com.kkulmoo.rebirth.card.infrastructure.entity;

import com.kkulmoo.rebirth.analysis.domain.enums.BenefitType;
import com.kkulmoo.rebirth.card.domain.DiscountType;
import com.kkulmoo.rebirth.shared.entity.CardTemplateEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;
import java.util.List;

@Entity
@Table(name = "benefit_templates")
@Getter
@Setter
@Builder
@Data
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
	@JdbcTypeCode(Types.ARRAY)
	private List<Integer> categoryIds;

	@Column(name = "subcategory_id", columnDefinition = "integer[]")
	@JdbcTypeCode(Types.ARRAY)
	private List<Integer> subcategoryIds;

	@Column(name = "benefit_type", nullable = false)
	@Enumerated(EnumType.STRING)
	private BenefitType benefitType;

	@Column(name = "merchant_filter_type", nullable = false)
	private Short merchantFilterType;

	@Column(name = "benefit_condition_type", nullable = false)
	private Short benefitConditionType;

	@Column(name = "performance_range", nullable = false, columnDefinition = "integer[]")
	@JdbcTypeCode(Types.ARRAY)
	private List<Integer> performanceRange;

	@Column(name = "benefits_by_section", columnDefinition = "double precision[]")
	@JdbcTypeCode(Types.ARRAY)
	private List<Double> benefitsBySection;

	@Column(name = "merchant_info", nullable = false)
	private Boolean merchantInfo;

	// merchant_list 컬럼을 text[] 에서 integer[]로 변경한 부분입니다.
	@Column(name = "merchant_list", columnDefinition = "integer[]")
	@JdbcTypeCode(Types.ARRAY)
	private List<Integer> merchantList;

	@Column(name = "payment_range", columnDefinition = "integer[]")
	@JdbcTypeCode(Types.ARRAY)
	private List<Integer> paymentRange;

	@Column(name = "benefit_usage_limit", columnDefinition = "smallint[]")
	@JdbcTypeCode(Types.ARRAY)
	private List<Short> benefitUsageLimit;

	@Column(name = "benefit_usage_amount", columnDefinition = "smallint[]")
	@JdbcTypeCode(Types.ARRAY)
	private List<Short> benefitUsageAmount;

	@Column(name = "discount_type")
	@Enumerated(EnumType.STRING)
	private DiscountType discountType;

	@Column(name = "additional_info")
	private String additionalInfo;
}