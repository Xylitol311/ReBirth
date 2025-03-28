package com.cardissuer.cardissuer.cards.infrastructure;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenJpaRepository extends JpaRepository<PermanentTokenEntity, String> {
	Optional<PermanentTokenEntity> findByCardUniqueNumberAndIsActiveTrue(String cardUniqueNumber);
}
