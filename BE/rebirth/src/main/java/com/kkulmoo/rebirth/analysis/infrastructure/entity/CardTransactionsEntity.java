package com.kkulmoo.rebirth.analysis.infrastructure.entity;

import com.kkulmoo.rebirth.analysis.domain.enums.BenefitType;
import com.kkulmoo.rebirth.analysis.domain.enums.StatusType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity
@Table(name = "card_transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardTransactionsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private int transactionId;

    @Column(name = "card_company_id")
    private int cardCompanyId;

    @Column(name = "merchant_id")
    private int merchantId;

    @Column(name = "card_unique_number")
    private String cardUniqueNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private StatusType status;

    @Enumerated(EnumType.STRING)
    @Column(name = "benefit_type")
    private BenefitType benefitType;

    @Column(name = "benefit_amount")
    private int benefitAmount;
}
