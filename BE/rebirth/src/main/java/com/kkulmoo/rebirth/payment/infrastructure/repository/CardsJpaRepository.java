package com.kkulmoo.rebirth.payment.infrastructure.repository;

import com.kkulmoo.rebirth.shared.entity.CardsEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

// 사용자 보유카드 jpa 연결
public interface CardsJpaRepository extends JpaRepository<CardsEntity, Integer>{

    List<CardsEntity> findByUserId(int userId);
    @Query("SELECT c.cardTemplateId FROM CardsEntity c WHERE c.permanentToken = :permanentToken")
    Optional<Integer> findCardTemplateIdByPermanentToken(@Param("permanentToken") String permanentToken);

}
