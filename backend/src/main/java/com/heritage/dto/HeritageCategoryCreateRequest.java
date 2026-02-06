package com.heritage.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class HeritageCategoryCreateRequest {

    @NotBlank(message = "类型名称不能为空")
    private String name;

    private Long parentId;

    private Integer sortOrder;
}

