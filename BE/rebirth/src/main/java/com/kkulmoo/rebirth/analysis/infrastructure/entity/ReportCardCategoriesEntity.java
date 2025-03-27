package com.kkulmoo.rebirth.analysis.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "report_card_categories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportCardCategoriesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_category_id")
    private int reportCategoryId;

    @Column(name = "report_card_id")
    private int reportCardId;

    @Column(name = "category_id")
    private int categoryId;

    @Column(name = "merchant_id")
    private int merchantId;

    @Column(name = "amount")
    private int amount;

    @Column(name = "received_benefit_amount")
    private int receivedBenefitAmount;

    @Column(name = "count")
    private int count;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
