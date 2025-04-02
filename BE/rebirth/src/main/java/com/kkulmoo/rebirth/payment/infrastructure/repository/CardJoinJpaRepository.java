package com.kkulmoo.rebirth.payment.infrastructure.repository;

import com.kkulmoo.rebirth.payment.domain.repository.CardJoinRepository;
import com.kkulmoo.rebirth.payment.infrastructure.dto.MyCardDto;
import com.kkulmoo.rebirth.shared.entity.CardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CardJoinJpaRepository extends JpaRepository<CardEntity, Integer>, CardJoinRepository {
    @Override
    @Query(
            "SELECT new com.kkulmoo.rebirth.payment.infrastructure.dto.MyCardDto(c.cardId, c.cardTemplateId, c.permanentToken, c.spendingTier) " +
            "FROM CardEntity c " +
            "WHERE c.userId = :userId"
    )
    List<MyCardDto> findMyCardIdAndTemplateIdByUserId(Integer userId);
}
