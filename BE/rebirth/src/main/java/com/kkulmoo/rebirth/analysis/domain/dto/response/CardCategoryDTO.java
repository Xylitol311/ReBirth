package com.kkulmoo.rebirth.analysis.domain.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CardCategoryDTO {

    String category;
    int amount;
    int benefit;
    int count;
}
