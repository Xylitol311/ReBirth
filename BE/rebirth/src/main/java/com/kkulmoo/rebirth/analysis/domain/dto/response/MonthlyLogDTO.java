package com.kkulmoo.rebirth.analysis.domain.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class MonthlyLogDTO {

    int day;
    int plus;
    int minus;
}
