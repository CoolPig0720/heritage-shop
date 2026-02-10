package com.heritage.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemVO {

    private Long id;

    private Long productId;

    private String productName;

    private String coverImageUrl;

    private BigDecimal unitPrice;

    private Integer quantity;

    private BigDecimal subtotal;
}
