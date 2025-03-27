package com.kkulmoo.rebirth.payment.domain.repository;

import com.kkulmoo.rebirth.payment.domain.Cards;

import java.util.List;

public interface CardsRepository {
    List<Cards> findByUserId(int userId);
    int findCardTemplateIdByToken(String permanentToken);
}
