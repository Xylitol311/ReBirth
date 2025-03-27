package com.kkulmoo.rebirth.payment.domain.repository;

public interface DisposableTokenRepository {
    String findByToken(String token);
    void saveToken(String id, String token);
}

