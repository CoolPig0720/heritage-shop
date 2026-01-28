package com.heritage.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class ProductImageUpdateRequest {

    private String imageUrl;

    @PositiveOrZero(message = "排序值不能为负数")
    private Integer sortOrder;

    @Min(value = 0, message = "封面标记不合法")
    @Max(value = 1, message = "封面标记不合法")
    private Integer isCover;
}
