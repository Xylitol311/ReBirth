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

    @JsonProperty("card_id")
    private Integer cardId;

    @JsonProperty("card_name")
    private String cardName;

    @JsonProperty("card_info")
    private String cardInfo;

    @JsonProperty("img_url")
    private String imgUrl;

    @JsonProperty("score")
    private Integer score;

    @JsonProperty("constellation")
    private String constellation;

}
