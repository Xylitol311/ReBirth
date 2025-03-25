package com.kkulmoo.rebirth.payment.infrastructure.repository;


import com.kkulmoo.rebirth.payment.domain.DisposableTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
public class DisposableTokenRepositoryImpl implements DisposableTokenRepository {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public String findByToken(String token) {
        // 토큰을 key로 사용하여 id 조회
        return redisTemplate.opsForValue().get(token);
    }

    @Override
    public void saveToken(String token, String id) {
        // 토큰을 key로, id를 value로 저장 (30분 후 자동 삭제)
        redisTemplate.opsForValue().set(token, id, 5, TimeUnit.MINUTES);
    }
}
