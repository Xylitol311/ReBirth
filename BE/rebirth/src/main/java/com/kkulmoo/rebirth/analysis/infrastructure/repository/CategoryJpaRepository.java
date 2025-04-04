package com.kkulmoo.rebirth.analysis.infrastructure.repository;

import com.kkulmoo.rebirth.transactions.infrastructure.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryJpaRepository extends JpaRepository<CategoryEntity, Integer> {

    @Query("SELECT c.categoryName FROM CategoryEntity c WHERE c.categoryId IN :categoryIds ORDER BY c.categoryId ASC")
    List<String> findByCategoryIdInOrderByCategoryId(
            @Param("categoryIds")List<Integer> categoryIds);
}
