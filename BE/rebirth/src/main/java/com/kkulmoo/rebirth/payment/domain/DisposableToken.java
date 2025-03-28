package com.kkulmoo.rebirth.payment.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
public class DisposableToken {

    String id;
    String token;

    @Builder
    public DisposableToken(String id, String token) {
        this.id = id;
        this.token = token;
    }
}
