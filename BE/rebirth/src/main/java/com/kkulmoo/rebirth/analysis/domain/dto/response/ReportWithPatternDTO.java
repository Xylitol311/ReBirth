package com.kkulmoo.rebirth.analysis.domain.dto.response;

import com.kkulmoo.rebirth.analysis.infrastructure.entity.ConsumptionPatternEntity;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ReportWithPatternDTO {
    int totalSpendingAmount;
    int preTotalSpendingAmount;
    int totalBenefitAmount;
    int totalGroupBenefitAverage;
    String reportDescription;
    ConsumptionPatternEntity consumptionPattern;
}
