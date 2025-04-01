package com.kkulmoo.rebirth.payment.domain.repository;

import com.kkulmoo.rebirth.payment.domain.paymentCard;

import java.util.List;

public interface CardsRepository {
    List<paymentCard> findByUserId(int userId);
    int findCardTemplateIdByToken(String permanentToken);
}
