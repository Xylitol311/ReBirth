package com.cardissuer.cardissuer.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
	// 기본 CRUD 메서드는 JpaRepository에서 제공됩니다
	// 필요한 경우 추가 쿼리 메서드를 여기에 정의할 수 있습니다
}