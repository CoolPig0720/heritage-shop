package com.heritage.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ProductImageBatchSortRequest {

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotEmpty(message = "排序项不能为空")
    @Valid
    private List<SortItem> items;

    @Data
    public static class SortItem {

        @NotNull(message = "图片ID不能为空")
        private Long imageId;

        @NotNull(message = "排序值不能为空")
        @Min(value = 0, message = "排序值不能为负数")
        private Integer sortOrder;
    }
}
