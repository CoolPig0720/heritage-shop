package com.heritage.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartSummaryVO {

    private Integer totalCount;

    private Integer selectedCount;

    private BigDecimal selectedAmount;
}
