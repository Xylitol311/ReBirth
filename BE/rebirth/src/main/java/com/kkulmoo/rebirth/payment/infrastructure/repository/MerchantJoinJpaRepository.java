package com.kkulmoo.rebirth.payment.infrastructure.repository;

import com.kkulmoo.rebirth.payment.domain.repository.MerchantJoinRepository;
import com.kkulmoo.rebirth.payment.infrastructure.dto.MerchantJoinDto;
import com.kkulmoo.rebirth.transactions.infrastructure.entity.MerchantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MerchantJoinJpaRepository extends JpaRepository<MerchantEntity, Long>, MerchantJoinRepository {
    @Override
    @Query("SELECT new com.kkulmoo.rebirth.payment.infrastructure.dto.MerchantJoinDto(m.merchantId, s.subcategoryId, c.categoryId) " +
            "FROM MerchantEntity m " +
            "JOIN m.subcategory s " +
            "JOIN s.category c " +
            "WHERE m.merchantName = :merchantName"
    )
    MerchantJoinDto findMerchantJoinDataByMerchantName(String merchantName);
}
