package com.kkulmoo.rebirth.recommend.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AvgAmountByCategoryDTO {

    @JsonProperty("category_id")
    Integer categoryId;

    @JsonProperty("category_name")
    String categoryName;

    @JsonProperty("avg_total_spending")
    Integer avgTotalSpending;

    @JsonProperty("avg_total_benefit")
    Integer avgTotalBenefit;
}
