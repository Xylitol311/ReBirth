package com.kkulmoo.rebirth.payment.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class paymentCard {

    private int cardId;

    private int userId;

    private int cardTemplateId;

    private String cardNumber;

    private String cardUniqueNumber;

    private LocalDate expiryDate;

    private Short cardOrder;

    private LocalDateTime createdAt;

    private LocalDateTime deletedAt; // Soft Delete 적용

    private Boolean isExpired;

    private int annualFee;

    private String permanentToken;

    private Short paymentCardOrder;

    private LocalDateTime paymentCreatedAt;

}
