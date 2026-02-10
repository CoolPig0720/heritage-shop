package com.heritage.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentCreateRequest {

    @NotNull
    private Long orderId;

    private String channel;
}
