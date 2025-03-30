package com.kkulmoo.rebirth.payment.infrastructure.entity;

import jakarta.persistence.Id;
import lombok.Getter;

@Getter
public class DisposableTokenEntity {

    private String id;
    private String token;
}
