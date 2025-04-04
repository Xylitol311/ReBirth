package com.kkulmoo.rebirth.transactions.domain;

import lombok.Getter;

@Getter
public enum BankTransactionType {
    DEPOSIT("입금"),
    WITHDRAWAL("출금");

    private final String koreanDescription;

    BankTransactionType(String koreanDescription) {
        this.koreanDescription = koreanDescription;
    }

    public String getKoreanDescription() {
        return koreanDescription;
    }
}
