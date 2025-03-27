package com.cardissuer.cardissuer.transaction.infrastrucuture;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cardissuer.cardissuer.transaction.infrastrucuture.CardTransactionEntity;

@Repository
public interface CardTransactionJpaRepository extends JpaRepository<CardTransactionEntity, Integer> {
	// 사용자 ID와 특정 시간 이후의 거래내역을 찾는 메소드
	// findBy + [Card의 속성] + [조건] + And + [Transaction의 속성] + [조건] + OrderBy + [정렬기준속성] + [정렬방향]
	List<CardTransactionEntity> findByCard_UserCIAndCreatedAtAfterOrderByCreatedAtDesc(String userId, Timestamp timestamp);
}
