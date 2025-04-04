package com.kkulmoo.rebirth.payment.domain.repository;

import com.kkulmoo.rebirth.payment.domain.CardTemplate;
import com.kkulmoo.rebirth.recommend.domain.dto.request.SearchParameterDTO;
import com.kkulmoo.rebirth.shared.entity.CardTemplateEntity;

import java.util.List;

public interface CardTemplateRepository {

    CardTemplate getCardTemplate(int cardTemplateId);

    List<CardTemplateEntity> searchCard(SearchParameterDTO searchParameterDTO);
}
