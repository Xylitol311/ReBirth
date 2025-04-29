package com.kkulmoo.rebirth.transactions.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "category")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CategoryEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "category_id")
	private Integer categoryId;

	@Column(name = "category_name", nullable = false, length = 100)
	private String categoryName;

	@OneToMany(mappedBy = "category")
	private List<SubcategoryEntity> subcategories;

}