package com.kkulmoo.rebirth.payment.domain;

import java.util.List;

public interface CardsRepository {
    List<Cards> findByUserId(int userId);

}
