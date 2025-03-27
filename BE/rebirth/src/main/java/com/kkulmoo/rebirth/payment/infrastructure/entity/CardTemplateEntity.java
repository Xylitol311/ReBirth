
package com.kkulmoo.rebirth.payment.infrastructure.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

    @Column(name = "card_deity_name", length = 50)
    private String cardDeityName;

    @Column(name = "card_deity_img_url", length = 255)
    private String cardDeityImgUrl;

    @Column(name = "annual_fee", nullable = false)
    private int annualFee;

    @Column(name = "card_type", length = 10, nullable = false)
    private String cardType;

    @Column(name = "card_detail_info", columnDefinition = "TEXT")
    private String cardDetailInfo;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "card_constellation_info", columnDefinition = "jsonb")
    private String cardConstellationInfo;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "benefit_conditions", columnDefinition = "jsonb")
    private String benefitConditions;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "last_month_usage_ranges", columnDefinition = "jsonb")
    private String lastMonthUsageRanges;

    @Column(name = "god_img_url", length = 255)
    private String godImgUrl;

    @Column(name = "god_name", length = 10)
    private String godName;

}

