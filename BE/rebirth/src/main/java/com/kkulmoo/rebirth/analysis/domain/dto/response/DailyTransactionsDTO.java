package com.kkulmoo.rebirth.analysis.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class DailyTransactionsDTO {

    @JsonProperty("date")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime date;

    @JsonProperty("category_name")
    String categoryName;

    @JsonProperty("merchant_name")
    String merchantName;

    int amount;

    @JsonProperty("card_name")
    String cardName;

}
