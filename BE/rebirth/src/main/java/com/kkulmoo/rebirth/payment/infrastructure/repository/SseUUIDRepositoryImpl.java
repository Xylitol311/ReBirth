package com.kkulmoo.rebirth.payment.infrastructure.repository;

import com.kkulmoo.rebirth.payment.domain.repository.DisposableTokenRepository;
import com.kkulmoo.rebirth.payment.domain.repository.SseUUIDRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
public class SseUUIDRepositoryImpl implements SseUUIDRepository {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;


    @Override
    public String findById(String id) {
        return redisTemplate.opsForValue().get(id);
    }

    @Override
    public void saveUuid(String id, String uuid) {
        redisTemplate.opsForValue().set(id, uuid,5, TimeUnit.MINUTES);
    }

    @Override
    public boolean deleteUuid(String id) {
        return redisTemplate.delete(id);
    }


}
