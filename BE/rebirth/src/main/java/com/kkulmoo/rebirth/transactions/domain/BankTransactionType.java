package com.kkulmoo.rebirth.transactions.domain;

public enum BankTransactionType {
    DEPOSIT("입금"),
    DEBIT("출금");

    private final String koreanDescription;

    BankTransactionType(String koreanDescription) {
        this.koreanDescription = koreanDescription;
    }

    public String getKoreanDescription() {
        return koreanDescription;
    }
}
