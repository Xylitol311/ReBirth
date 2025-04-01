package com.cardissuer.cardissuer.cards.infrastructure;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardJpaRepository extends JpaRepository<CardEntity, String> {
	List<CardEntity> findByUserCIAndDeletedAtIsNull(String userCI);

	boolean existsByCardUniqueNumberAndUserCI(String cardUniqueNumber, String userCI);

}
