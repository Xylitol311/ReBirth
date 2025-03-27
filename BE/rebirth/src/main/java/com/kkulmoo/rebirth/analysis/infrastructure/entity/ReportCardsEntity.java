package com.kkulmoo.rebirth.analysis.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "report_cards")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportCardsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_card_id")
    private int reportCardId;

    @Column(name = "card_id")
    private int cardId;

    @Column(name = "report_id")
    private int reportId;

    @Column(name = "month_spending_amount")
    private int monthSpendingAmount;

    @Column(name = "month_benefit_amount")
    private int monthBenefitAmount;

    @Column(name = "spending_tier")
    private int spendingTier;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
