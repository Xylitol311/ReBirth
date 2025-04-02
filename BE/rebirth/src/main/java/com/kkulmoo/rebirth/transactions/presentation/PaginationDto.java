package com.kkulmoo.rebirth.transactions.presentation;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaginationDto {
    private Integer currentPage;
    private Integer pageSize;
    private Boolean hasMore;
}