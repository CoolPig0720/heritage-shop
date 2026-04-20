package com.heritage.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MerchantProductBriefVO {

    private Long id;

    private String name;

    private BigDecimal price;

    private BigDecimal avgRating;

    private String coverImageUrl;
}
