package com.kkulmoo.rebirth.analysis.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "monthly_transaction_summary")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyTransactionSummaryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private int reportId;

    @Column(name = "user_id")
    private int userId;

    @Column(name = "year")
    private int year;

    @Column(name = "month")
    private int month;

    @Column(name = "total_spending")
    private int totalSpending;

    @Column(name = "received_benefit_amount")
    private int receivedBenefitAmount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
