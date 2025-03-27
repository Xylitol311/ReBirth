package com.kkulmoo.rebirth.analysis.domain.dto.response;

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
    int totalCount;
    int totalAmount;
    int totalBenefit;
    List<CardCategoryDTO> categories;
}
