package com.kkulmoo.rebirth.card.infrastrucuture.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kkulmoo.rebirth.card.infrastrucuture.entity.CardTemplateEntity;

public interface CardTemplateJpaRepository extends JpaRepository<CardTemplateEntity, Integer> {
	Optional<CardTemplateEntity> findByCardName(String cardName);
	List<CardTemplateEntity> findByCardNameContaining(String cardName);
	List<CardTemplateEntity> findByCardNameIn(Collection<String> cardNames);

}

