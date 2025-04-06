package com.kkulmoo.rebirth.payment.infrastructure.entity;

import com.kkulmoo.rebirth.analysis.domain.enums.BenefitType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pre_benefits")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PreBenefitEntity {
    @Id
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "payment_card_id", nullable = false)
    private Integer paymentCardId;

    @Column(name = "recommended_card_id", nullable = false)
    private Integer recommendedCardId;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "if_benefit_type")
    private BenefitType ifBenefitType;

    @Column(name = "if_benefit_amount")
    private Integer ifBenefitAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "real_benefit_type")
    private BenefitType realBenefitType;

    @Column(name = "real_benefit_amount")
    private Integer realBenefitAmount;

    @Column(name = "merchant_name", length = 100)
    private String merchantName;
}
