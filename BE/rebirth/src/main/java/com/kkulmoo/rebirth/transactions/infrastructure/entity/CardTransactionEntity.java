package com.kkulmoo.rebirth.transactions.infrastructure.entity;


import com.kkulmoo.rebirth.analysis.domain.enums.BenefitType;
import com.kkulmoo.rebirth.transactions.domain.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "card_transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardTransactionEntity {
    @Id
    @Column(name = "transaction_id")
    private Integer transactionId;

    @Column(name = "merchant_id")
    private Integer merchantId;

    @Column(name = "card_unique_number")
    private String cardUniqueNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "benefit_type")
    private BenefitType cardBenefitType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;

    @Column(name = "benefit_amount")
    private Integer benefitAmount;

    @Column(name = "benefit_id")
    private Integer benefitId;
}
