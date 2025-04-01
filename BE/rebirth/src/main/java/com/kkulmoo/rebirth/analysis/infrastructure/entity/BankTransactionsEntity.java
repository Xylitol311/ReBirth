package com.kkulmoo.rebirth.analysis.infrastructure.entity;

import com.kkulmoo.rebirth.analysis.domain.enums.TransactionType;
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
    private int transactionId;

    @Column(name = "card_company_id")
    private int cardCompanyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type")
    private TransactionType transactionType;

    @Column(name = "account_number", length = 30)
    private String accountNumber;

    @Column(name = "transaction_memo", length = 30)
    private String transactionMemo;
}
