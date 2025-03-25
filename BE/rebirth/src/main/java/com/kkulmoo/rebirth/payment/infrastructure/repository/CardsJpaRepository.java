package com.kkulmoo.rebirth.payment.infrastructure.repository;

import com.kkulmoo.rebirth.payment.infrastructure.entity.CardsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CardsJpaRepository extends JpaRepository<CardsEntity, Integer>{

    List<CardsEntity> findByUserId(int userId);
}
