package com.heritage.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class HeritageProjectMediaCreateRequest {

    @NotBlank(message = "媒体类型不能为空")
    private String mediaType;

    @NotBlank(message = "媒体地址不能为空")
    private String mediaUrl;

    private Integer sortOrder;
}
