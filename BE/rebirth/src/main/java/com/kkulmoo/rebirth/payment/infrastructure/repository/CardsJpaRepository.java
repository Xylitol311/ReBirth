package com.kkulmoo.rebirth.payment.infrastructure.repository;

import com.kkulmoo.rebirth.payment.infrastructure.entity.CardsEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CardsJpaRepository extends JpaRepository<CardsEntity, Integer>{

    List<CardsEntity> findByUserId(int userId);

    Optional<CardsEntity> findByPermanentToken(String permanentToken);

    @Query("SELECT c.cardTemplateId FROM cards c WHERE c.permanentToken = :permanentToken")
    Optional<Integer> findCardTemplateIdByPermanentToken(@Param("permanentToken") String permanentToken);

}
