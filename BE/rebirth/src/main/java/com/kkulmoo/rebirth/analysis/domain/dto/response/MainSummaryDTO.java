package com.kkulmoo.rebirth.analysis.domain.dto.response;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class MainSummaryDTO {

    private Integer totalSpendingAmount;
    private Integer totalBenefitAmount;
    List<ReportCategoryDTO> goodList;
    List<ReportCategoryDTO> badList;
}
