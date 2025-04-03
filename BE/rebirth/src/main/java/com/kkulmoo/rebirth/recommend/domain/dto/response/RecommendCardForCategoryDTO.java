package com.kkulmoo.rebirth.recommend.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class RecommendCardForCategoryDTO {

    private Integer categoryId;

    private String categoryName;

    List<RecommendCardDTO> recommendCards;
}
