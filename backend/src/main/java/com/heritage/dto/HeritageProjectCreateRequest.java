package com.heritage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HeritageProjectCreateRequest {

    @NotNull(message = "类型不能为空")
    private Long categoryId;

    @NotBlank(message = "项目名称不能为空")
    private String name;

    @NotBlank(message = "项目介绍不能为空")
    private String description;

    @NotBlank(message = "申报单位不能为空")
    private String applyUnit;

    @NotBlank(message = "保护单位不能为空")
    private String protectUnit;
}

