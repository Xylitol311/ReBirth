package com.kkulmoo.rebirth.card.domain;

public enum CardType {
    신용("CREDIT"),
    체크("DEBIT");

    private final String description;

    CardType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}