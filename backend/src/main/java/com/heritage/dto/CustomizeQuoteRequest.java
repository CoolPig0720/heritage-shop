package com.heritage.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CustomizeQuoteRequest {

    @NotNull(message = "报价不能为空")
    @DecimalMin(value = "0.01", message = "报价必须大于0")
    private BigDecimal quotedPrice;
}
