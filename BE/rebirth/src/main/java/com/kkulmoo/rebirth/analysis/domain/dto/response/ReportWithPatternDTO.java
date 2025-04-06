package com.kkulmoo.rebirth.analysis.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kkulmoo.rebirth.analysis.infrastructure.entity.ConsumptionPatternEntity;
import jakarta.persistence.Column;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ReportWithPatternDTO {

    @JsonProperty("total_spending_amount")
    Integer totalSpendingAmount;

    @JsonProperty("pre_total_spending_amount")
    Integer preTotalSpendingAmount;

    @JsonProperty("total_benefit_amount")
    Integer totalBenefitAmount;

    @JsonProperty("total_group_benefit_average")
    Integer totalGroupBenefitAverage;

    @JsonProperty("group_name")
    String groupName;

    @JsonProperty("over_consumption")
    Integer overConsumption;

    @JsonProperty("variation")
    Integer variation;

    @JsonProperty("extrovert")
    Integer extrovert;

    @JsonProperty("report_description")
    String reportDescription;

    @JsonProperty("consumption_patterns")
    ConsumptionPatternDTO consumptionPattern;
}
