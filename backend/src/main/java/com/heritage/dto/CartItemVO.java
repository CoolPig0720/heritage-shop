package com.heritage.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemVO {

    private Long id;

    private Long productId;

    private String productName;

    private String coverImageUrl;

    private BigDecimal price;

    private Integer productStatus;

    private Integer quantity;

    private Integer selected;
}
