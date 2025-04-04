package com.kkulmoo.rebirth.payment.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_card_benefit", schema = "public")
@IdClass(UserCardBenefitId.class)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserCardBenefitEntity {
    @Id
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Id
    @Column(name = "benefit_template_id", nullable = false)
    private Integer benefitTemplateId;

    @Column(name = "spending_tier")
    private Short spendingTier;

    @Column(name = "benefit_count", nullable = false)
    private Short benefitCount;

    @Column(name = "benefit_amount", nullable = false)
    private Integer benefitAmount;

    @Column(name = "reset_date", nullable = false)
    private LocalDateTime resetDate;

    @Column(name = "update_date", nullable = false)
    private LocalDateTime updateDate;
}
