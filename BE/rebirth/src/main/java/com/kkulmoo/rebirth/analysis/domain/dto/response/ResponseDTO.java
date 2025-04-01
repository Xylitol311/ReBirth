package com.kkulmoo.rebirth.analysis.domain.dto.response;

import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ResponseDTO {

    boolean success;
    String message;
    Object data;
}
