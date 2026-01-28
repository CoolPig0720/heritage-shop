package com.heritage.dto;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductUpdateRequest {

    private String name;

    private String description;

    @PositiveOrZero(message = "价格不能为负数")
    private BigDecimal price;

    private String traceCode;

    private String traceQrUrl;

    private String model3dUrl;

    private Integer status;
}

