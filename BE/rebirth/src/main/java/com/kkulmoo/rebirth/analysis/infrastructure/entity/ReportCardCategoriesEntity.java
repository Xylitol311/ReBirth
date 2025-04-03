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
    private Integer reportCategoryId;

    @JoinColumn(name = "report_card_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private ReportCardsEntity reportCard;

    @Column(name = "category_id")
    private Integer categoryId;

    @Column(name = "amount")
    private Integer amount;

    @Column(name = "received_benefit_amount")
    private Integer receivedBenefitAmount;

    @Column(name = "count")
    private Integer count;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
