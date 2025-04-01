package com.kkulmoo.rebirth.analysis.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class MonthlySpendingByCategoryAndCardDTO {

    @JsonProperty("category_id")
    int categoryId;

    @JsonProperty("card_id")
    int cardId;

    @JsonProperty("total_spending")
    int totalSpending;

    @JsonProperty("total_benefit")
    int totalBenefit;

    @JsonProperty("transaction_count")
    int transactionCount;
}
