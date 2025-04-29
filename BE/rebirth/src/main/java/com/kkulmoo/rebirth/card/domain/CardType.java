package com.kkulmoo.rebirth.card.domain;

public enum CardType {
    CREDIT("신용"),
    DEBIT("체크");

    private final String description;

    CardType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}