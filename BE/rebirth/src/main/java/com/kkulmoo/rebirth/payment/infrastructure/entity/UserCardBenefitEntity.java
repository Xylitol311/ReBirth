package com.kkulmoo.rebirth.payment.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_card_benefit", schema = "public")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserCardBenefitEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_card_benefit_id", nullable = false)
    private Integer userCardBenefitId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "benefit_template_id", nullable = false)
    private Integer benefitTemplateId;

    @Column(name = "spending_tier", nullable = false)
    private Short spendingTier;

    @Column(name = "benefit_count", nullable = false)
    private Short benefitCount;

    @Column(name = "benefit_amount", nullable = false)
    private Integer benefitAmount;

    @Column(name = "update_date", nullable = false)
    private LocalDateTime updateDate;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "month", nullable = false)
    private Integer month;
}