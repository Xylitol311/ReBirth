package com.kkulmoo.rebirth.payment.infrastructure.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Date;
import java.time.LocalDateTime;

@Entity
@Table(name ="cards")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder

public class CardsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "card_id")
    private int cardId;

    @Column(name="user_id", nullable = false, updatable = false)
    private int userId;

    @Column(name="card_template_id", nullable = false, updatable = false)
    private int cardTemplateId;

    @Column(name = "card_number", nullable = false, length = 255)
    private String cardNumber;

    @Column(name = "card_unique_number", nullable = false, length = 255, unique = true)
    private String cardUniqueNumber;

    @Column(name = "expiry_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date expiryDate;

    @Column(name = "card_order", nullable = false)
    private Short cardOrder;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt; // Soft Delete 적용

    @ColumnDefault("0")
    @Column(name = "is_expired", nullable = false)
    private Short isExpired;

    @Column(name = "annual_fee", nullable = false)
    private int annualFee;

    @Column(name = "permanent_token", length = 255)
    private String permanentToken;

    @Column(name = "payment_card_order")
    private Short paymentCardOrder;

    @Column(name = "payment_created_at")
    private LocalDateTime paymentCreatedAt;



}
