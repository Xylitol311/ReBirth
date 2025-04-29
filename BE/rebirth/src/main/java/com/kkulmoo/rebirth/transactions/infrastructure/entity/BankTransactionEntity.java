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
public class BankTransactionEntity {
    @Id
    @Column(name = "transaction_id")
    private Integer transactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type")
    private BankTransactionType bankTransactionType;

    @Column(name = "account_number")
    private String accountNumber;

}
