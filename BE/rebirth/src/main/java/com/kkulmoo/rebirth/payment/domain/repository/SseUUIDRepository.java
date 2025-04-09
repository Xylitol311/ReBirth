package com.kkulmoo.rebirth.payment.domain.repository;

public interface SseUUIDRepository {
    String findById(String id);
    void saveUuid(String id, String uuid);
    boolean deleteUuid(String id);
}