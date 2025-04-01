package com.kkulmoo.rebirth.transactions.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BankTransactions {
    Integer TransactionsId;
    Short cardCompanyId;
    BankTransactionType bankTransactionType;
    String accountNumber;
    String transaction_memo;
}
