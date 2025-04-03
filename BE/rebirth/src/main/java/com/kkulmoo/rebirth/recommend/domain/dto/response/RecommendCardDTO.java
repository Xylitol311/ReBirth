package com.kkulmoo.rebirth.recommend.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class RecommendCardDTO {

    private Integer cardId;

    private String cardName;

    private String cardInfo;

    private String imgUrl;

    private Integer score;

    private String constellation;

}
