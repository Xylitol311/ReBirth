package com.kkulmoo.rebirth.payment.domain;

import lombok.Builder;
import lombok.Getter;

import java.sql.Date;
import java.time.LocalDateTime;

@Getter
public class Cards {

    private int cardId;

    private int userId;

    private int cardTemplateId;

    private String cardNumber;

    private String cardUniqueNumber;

    private Date expiryDate;

    private Short cardOrder;

    private LocalDateTime createdAt;

    private LocalDateTime deletedAt; // Soft Delete 적용

    private Short isExpired;

    private int annualFee;

    private String permanentToken;

    private Short paymentCardOrder;

    private LocalDateTime paymentCreatedAt;

    @Builder
    public Cards(LocalDateTime paymentCreatedAt, Short paymentCardOrder, String permanentToken, int annualFee, Short isExpired, LocalDateTime deletedAt, LocalDateTime createdAt, Short cardOrder, Date expiryDate, String cardUniqueNumber, String cardNumber, int cardTemplateId, int userId, int cardId) {
        this.paymentCreatedAt = paymentCreatedAt;
        this.paymentCardOrder = paymentCardOrder;
        this.permanentToken = permanentToken;
        this.annualFee = annualFee;
        this.isExpired = isExpired;
        this.deletedAt = deletedAt;
        this.createdAt = createdAt;
        this.cardOrder = cardOrder;
        this.expiryDate = expiryDate;
        this.cardUniqueNumber = cardUniqueNumber;
        this.cardNumber = cardNumber;
        this.cardTemplateId = cardTemplateId;
        this.userId = userId;
        this.cardId = cardId;
    }
}
