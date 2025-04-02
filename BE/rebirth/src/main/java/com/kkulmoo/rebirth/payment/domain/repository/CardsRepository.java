package com.kkulmoo.rebirth.payment.domain.repository;

import com.kkulmoo.rebirth.payment.domain.PaymentCard;

import java.util.List;

public interface CardsRepository {
    List<PaymentCard> findByUserId(int userId);
    int findCardTemplateIdByToken(String permanentToken);
}
