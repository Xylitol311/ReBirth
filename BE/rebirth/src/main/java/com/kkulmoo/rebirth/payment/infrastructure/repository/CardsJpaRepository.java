package com.kkulmoo.rebirth.payment.infrastructure.repository;

import com.kkulmoo.rebirth.payment.domain.PaymentCard;
import com.kkulmoo.rebirth.shared.entity.CardEntity;
import com.kkulmoo.rebirth.shared.entity.CardTemplateEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

// 사용자 보유카드 jpa 연결
public interface CardsJpaRepository extends JpaRepository<CardEntity, Integer>{

    List<CardEntity> findByUserId(int userId);
    @Query("SELECT c.cardTemplateId FROM CardEntity c WHERE c.permanentToken = :permanentToken")
    Optional<Integer> findCardTemplateIdByPermanentToken(@Param("permanentToken") String permanentToken);

    @Query("SELECT ct " +
            "FROM CardTemplateEntity ct " +
            "JOIN CardEntity c ON ct.cardTemplateId = c.cardTemplateId " +
            "WHERE c.cardId = :cardId")
    CardTemplateEntity findCardNameByCardId(int cardId);

    List<CardEntity> getByUserId(int userId);

    List<CardEntity> findByUserIdAndPaymentCardOrderIsNotNull(Integer userId);

    CardEntity findByUserIdAndCardUniqueNumber(Integer userId, String cardUniqueNumber);
}
