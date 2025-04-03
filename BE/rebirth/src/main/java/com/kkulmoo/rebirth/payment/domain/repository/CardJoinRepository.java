package com.kkulmoo.rebirth.payment.domain.repository;

import com.kkulmoo.rebirth.payment.infrastructure.dto.MyCardDto;

import java.util.List;

public interface CardJoinRepository {
    List<MyCardDto> findMyCardsIdAndTemplateIdsByUserId(Integer userId);
}
