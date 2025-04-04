package com.kkulmoo.rebirth.recommend.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class SearchParameterDTO {

    List<String> benefitType;
    List<String> cardCompany;
    List<String> category;

    Integer minPerformanceRange;
    Integer maxPerformanceRange;

    Integer minAnnualFee;
    Integer maxAnnualFee;
}
