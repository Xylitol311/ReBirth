package com.kkulmoo.rebirth.payment.infrastructure.repository;

import com.kkulmoo.rebirth.payment.domain.CardTemplate;
import com.kkulmoo.rebirth.payment.domain.repository.CardTemplateRepository;
import com.kkulmoo.rebirth.recommend.domain.dto.request.SearchParameterDTO;
import com.kkulmoo.rebirth.shared.entity.CardTemplateEntity;
import com.kkulmoo.rebirth.payment.infrastructure.mapper.CardTemplateEntityMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CardTemplateRepositoryImpl implements CardTemplateRepository {

    private final CardTemplateJpaRepository cardsTemplateJpaRepository;
    private final CardTemplateEntityMapper cardTemplateEntityMapper;

    public CardTemplateRepositoryImpl(CardTemplateJpaRepository cardsTemplateJpaRepository, CardTemplateEntityMapper cardTemplateEntityMapper) {
        this.cardsTemplateJpaRepository = cardsTemplateJpaRepository;
        this.cardTemplateEntityMapper = cardTemplateEntityMapper;
    }


    @Override
    public CardTemplate getCardTemplate(int cardTemplateId) {

        Optional<CardTemplateEntity> cardTemplateEntity = cardsTemplateJpaRepository.findById(cardTemplateId);
        if(cardTemplateEntity.isEmpty()) return null;

        return cardTemplateEntityMapper.toCardTemplate(cardTemplateEntity.orElseThrow());
    }

    @Override
    public List<CardTemplateEntity> searchCard(SearchParameterDTO searchParameterDTO) {
        return cardsTemplateJpaRepository.searchCards(searchParameterDTO.getBenefitType(),
                searchParameterDTO.getCardCompany(),
                searchParameterDTO.getCategory(),
                searchParameterDTO.getMinPerformanceRange(),
                searchParameterDTO.getMaxPerformanceRange(),
                searchParameterDTO.getMinAnnualFee(),
                searchParameterDTO.getMaxAnnualFee());


    }
}
