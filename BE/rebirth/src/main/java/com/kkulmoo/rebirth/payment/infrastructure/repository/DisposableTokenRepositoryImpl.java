package com.kkulmoo.rebirth.payment.infrastructure.repository;


import com.kkulmoo.rebirth.payment.domain.repository.DisposableTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

// 일회용 토큰 redis 저장용도
@Repository
public class DisposableTokenRepositoryImpl implements DisposableTokenRepository {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public String findById(String id) {
        // 토큰을 key로 사용하여 id 조회
        return redisTemplate.opsForValue().get(id);
    }

    @Override
    public void saveToken(String id, String token) {
        // 토큰을 key로, id를 value로 저장 (30분 후 자동 삭제)
        redisTemplate.opsForValue().set(id, token,5, TimeUnit.MINUTES);
    }
}
