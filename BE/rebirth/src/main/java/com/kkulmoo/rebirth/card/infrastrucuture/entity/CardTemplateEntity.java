package com.kkulmoo.rebirth.card.infrastrucuture.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "card_templates")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CardTemplateEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "card_template_id")
	private Integer cardTemplateId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "card_company_id", nullable = false)
	private CardCompanyEntity cardCompany;

	@Column(name = "card_name", nullable = false, length = 100)
	private String cardName;

	@Column(name = "card_img_url", nullable = false, length = 255)
	private String cardImgUrl;

	@Column(name = "god_name", length = 10)
	private String godName;

	@Column(name = "god_img_url", length = 255)
	private String godImgUrl;

	@Column(name = "annual_fee", nullable = false)
	private Integer annualFee;

	@Column(name = "card_type", nullable = false)
	private String cardType;

	@Column(name = "spending_max_tier", nullable = false)
	private Short spendingMaxTier;

	@Column(name = "max_performance_amount", nullable = false)
	private Integer maxPerformanceAmount;

	@Column(name = "benefit_text")
	private String benefitText;

}