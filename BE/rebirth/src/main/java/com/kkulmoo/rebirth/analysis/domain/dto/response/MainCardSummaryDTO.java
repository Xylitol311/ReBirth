package com.kkulmoo.rebirth.analysis.domain.dto.response;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class MainCardSummaryDTO {

    private String cardName;
    private String cardImgUrl;
    private Integer spendingAmount;
    private Integer benefitAmount;
    private Integer annualFee;
}
