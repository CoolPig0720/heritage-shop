package com.heritage.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CustomizeConfirmRequest {

    @NotNull(message = "收件地址不能为空")
    private Long addressId;
}
