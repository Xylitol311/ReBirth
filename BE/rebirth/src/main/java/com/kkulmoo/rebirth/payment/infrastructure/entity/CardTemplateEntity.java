package com.kkulmoo.rebirth.payment.infrastructure.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "card_templates")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardTemplateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "card_template_id", nullable = false)
    private int cardTemplateId;

    @Column(name = "card_company_id", nullable = false)
    private Short cardCompanyId;

    @Column(name = "card_name", length = 100, nullable = false)
    private String cardName;

    @Column(name = "card_img_url", length = 255, nullable = false)
    private String cardImgUrl;

    @Column(name = "god_name", length = 10)
    private String godName;

    @Column(name = "god_img_url", length = 255)
    private String godImgUrl;

    @Column(name = "annual_fee", nullable = false)
    private int annualFee = 0;

    @Column(name = "card_type", length = 10, nullable = false)
    private String cardType;

    @Column(name = "spending_max_tier", nullable = false)
    private Short spendingMaxTier;

    @Column(name = "max_performance_amount", nullable = false)
    private int maxPerformanceAmount;
}
