package com.kkulmoo.rebirth.analysis.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class MonthlyLogDTO {

    @JsonProperty("day")
    int day;

    @JsonProperty("minus")
    int minus;

    @JsonProperty("plus")
    int plus;
}
