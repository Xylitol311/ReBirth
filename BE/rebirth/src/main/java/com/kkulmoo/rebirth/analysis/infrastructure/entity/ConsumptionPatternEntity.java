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
@Table(name = "consumption_pattern")
@NoArgsConstructor
@AllArgsConstructor
public class ConsumptionPatternEntity {

    @Id
    @Column(name = "consumption_pattern_id")
    private String consumptionPatternId;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "img_url")
    private String imgUrl;
}
