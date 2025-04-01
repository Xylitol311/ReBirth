package com.kkulmoo.rebirth.transactions.infrastructure.entity;

import com.kkulmoo.rebirth.transactions.domain.BankTransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "bank_transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankTransactionsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Integer transactionId;

    @Column(name = "card_company_id")
    private Short cardCompanyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type")
    private BankTransactionType bankTransactionType;

    @Column(name = "account_number")
    private String accountNumber;

}
