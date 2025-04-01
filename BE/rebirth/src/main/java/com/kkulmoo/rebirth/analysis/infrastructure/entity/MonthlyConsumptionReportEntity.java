package com.kkulmoo.rebirth.analysis.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "monthly_consumption_report")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyConsumptionReportEntity {

    @Id
    @Column(name = "report_id")
    private int reportId;

    @OneToOne
    @MapsId  // reportId를 다른 테이블의 기본 키와 공유
    @JoinColumn(name = "report_id")  // 외래 키 매핑
    private MonthlyTransactionSummaryEntity report;  // 1대1 관계 엔티티

    @Column(name = "consumption_pattern_id")
    private String consumptionPatternId;

    @Column(name = "report_description")
    private String reportDescription;

    @Column(name = "over_consumption")
    private int overConsumption;

    @Column(name = "variation")
    private int variation;

    @Column(name = "extrovert")
    private int extrovert;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
