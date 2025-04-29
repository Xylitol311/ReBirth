package com.kkulmoo.rebirth.analysis.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kkulmoo.rebirth.analysis.infrastructure.entity.ConsumptionPatternEntity;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ConsumptionPatternDTO {
    @JsonProperty("pattern_id")
    private String id;

    @JsonProperty("pattern_name")
    private String patternName;

    @JsonProperty("description")
    private String description;

    @JsonProperty("img_url")
    private String imgUrl;

    public ConsumptionPatternDTO(ConsumptionPatternEntity entity) {
        this.id = entity.getConsumptionPatternId();
        this.patternName = entity.getName();
        this.description = entity.getDescription();
        this.imgUrl = entity.getImgUrl();
    }
}
