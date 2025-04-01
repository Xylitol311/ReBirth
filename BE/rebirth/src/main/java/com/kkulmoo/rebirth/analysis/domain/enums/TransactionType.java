package com.kkulmoo.rebirth.analysis.domain.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape= JsonFormat.Shape.OBJECT)
public enum TransactionType {

    deposit("입금"),
    withdrawal("출금");

    final private String value;

    TransactionType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static TransactionType fromValue(String value) {
        for (TransactionType transactionType : values()) {
            if (transactionType.value.equals(value)) {
                return transactionType;
            }
        }
        throw new IllegalArgumentException("Unknown transaction_type value: " + value);
    }
}
