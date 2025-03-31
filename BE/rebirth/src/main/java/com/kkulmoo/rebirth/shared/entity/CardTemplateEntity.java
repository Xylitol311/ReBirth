
package com.kkulmoo.rebirth.shared.entity;

import com.kkulmoo.rebirth.card.domain.CardType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Column(name = "annual_fee", nullable = false)
    private int annualFee;

    @Column(name = "card_detail_info", columnDefinition = "TEXT")
    private String cardDetailInfo;

    @Column(name = "card_type", length = 10, nullable = false)
    @Enumerated(EnumType.STRING)
    private CardType cardType;

    @Column(name = "card_constellation_info", columnDefinition = "JSONB")
    private String cardConstellationInfo;
}

