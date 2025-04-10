package com.kkulmoo.rebirth.analysis.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ReportCardDTO {

    String name;

    String cardImg;

    @JsonProperty("total_count")
    int totalCount;

    @JsonProperty("total_amount")
    int totalAmount;

    @JsonProperty("total_benefit")
    int totalBenefit;

    List<CardCategoryDTO> categories;
}
