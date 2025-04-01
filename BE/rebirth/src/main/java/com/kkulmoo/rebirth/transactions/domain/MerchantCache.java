package com.kkulmoo.rebirth.transactions.domain;

import com.kkulmoo.rebirth.transactions.infrastructure.entity.MerchantEntity;
import com.kkulmoo.rebirth.transactions.infrastructure.repository.MerchantJpaRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class MerchantCache {
    private final MerchantJpaRepository merchantJpaRepository;
    private Map<String, Integer> merchantNameToIdMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        refreshCache();
    }

    @Scheduled(fixedRate = 3600000) // 1시간마다 갱신
    public void refreshCache() {
        log.info("가맹점 캐시 갱신 시작");
        long startTime = System.currentTimeMillis();

        List<MerchantEntity> merchants = merchantJpaRepository.findAll();
        Map<String, Integer> newMap = merchants.stream()
                .collect(Collectors.toConcurrentMap(
                        MerchantEntity::getMerchantName,
                        MerchantEntity::getMerchantId,
                        (v1, v2) -> v1 // 중복 시 처리
                ));

        merchantNameToIdMap = newMap;

        long endTime = System.currentTimeMillis();
        log.info("가맹점 캐시 갱신 완료. 소요시간: {}ms, 가맹점 수: {}", (endTime - startTime), merchants.size());
    }

    public Integer getMerchantIdByName(String name) {
        return merchantNameToIdMap.get(name);
    }
}
