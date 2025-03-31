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
    int totalSpendingAmount;

    @JsonProperty("category_name")
    String categoryName;

    @JsonProperty("monthly_difference")
    int monthlyDifferenceAmount;
}
