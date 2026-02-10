package com.heritage.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartItemQuantityUpdateRequest {

    @NotNull
    @Min(1)
    private Integer quantity;
}
