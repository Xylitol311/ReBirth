package com.kkulmoo.rebirth.recommend.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Top3CardDTO {

    private Integer amount;

    private List<RecommendCardDTO> recommendCards;
}
