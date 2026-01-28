package com.heritage.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductStatusUpdateRequest {

    @NotNull(message = "商品状态不能为空")
    @Min(value = 0, message = "商品状态不合法")
    @Max(value = 1, message = "商品状态不合法")
    private Integer status;
}

