package com.kkulmoo.rebirth.payment.domain.repository;

public interface DisposableTokenRepository {
    String findById(String token);
    void saveToken(String id, String token);
}

