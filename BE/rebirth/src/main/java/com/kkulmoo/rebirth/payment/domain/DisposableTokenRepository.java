package com.kkulmoo.rebirth.payment.domain;

public interface DisposableTokenRepository {
    String findByToken(String token);
    void saveToken(String id, String token);
}

