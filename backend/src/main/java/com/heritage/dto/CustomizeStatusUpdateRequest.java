package com.heritage.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CustomizeStatusUpdateRequest {

    @NotBlank(message = "状态不能为空")
    private String status;
}
