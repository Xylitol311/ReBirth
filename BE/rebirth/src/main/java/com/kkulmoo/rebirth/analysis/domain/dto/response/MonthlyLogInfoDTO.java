package com.kkulmoo.rebirth.analysis.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class MonthlyLogInfoDTO {

    @JsonProperty("total_spending_amount")
    Long totalSpendingAmount;

    @JsonProperty("category_name")
    String categoryName;

    @JsonProperty("monthly_difference")
    Long monthlyDifferenceAmount;
}
