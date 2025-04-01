package com.kkulmoo.rebirth.transactions.infrastructure.entity;


import com.kkulmoo.rebirth.transactions.domain.CardBenefitType;
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
public class CardTransactionsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Integer transactionId;

    @Column(name = "card_company_id")
    private Short cardCompanyId;

    @Column(name = "merchant_id")
    private Integer merchantId;

    @Column(name = "card_unique_number")
    private String cardUniqueNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(name = "benefit_type")
    private CardBenefitType cardBenefitType;

    @Column(name = "benefit_amount")
    private Integer benefitAmount;
}
