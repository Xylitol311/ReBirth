package com.kkulmoo.rebirth.analysis.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kkulmoo.rebirth.analysis.infrastructure.entity.ConsumptionPatternEntity;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ReportWithPatternDTO {

    @JsonProperty("total_spending_amount")
    int totalSpendingAmount;

    @JsonProperty("pre_total_spending_amount")
    int preTotalSpendingAmount;

    @JsonProperty("total_benefit_amount")
    int totalBenefitAmount;

    @JsonProperty("total_group_benefit_average")
    int totalGroupBenefitAverage;

    @JsonProperty("group_name")
    String groupName;

    @JsonProperty("report_description")
    String reportDescription;

    @JsonProperty("consumption_patterns")
    ConsumptionPatternDTO consumptionPattern;
}
