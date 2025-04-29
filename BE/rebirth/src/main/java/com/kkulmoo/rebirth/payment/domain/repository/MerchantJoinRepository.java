package com.kkulmoo.rebirth.payment.domain.repository;

import com.kkulmoo.rebirth.payment.infrastructure.dto.MerchantJoinDto;

public interface MerchantJoinRepository {
    MerchantJoinDto findMerchantJoinDataByMerchantName(String merchantName);
}
