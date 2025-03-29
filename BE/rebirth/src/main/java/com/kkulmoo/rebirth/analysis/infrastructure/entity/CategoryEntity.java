package com.kkulmoo.rebirth.analysis.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "category")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryEntity {

    @Id
    @Column(name = "category_id")
    private int categoryId;

    @Column(name = "category_name")
    private String categoryName;
}
