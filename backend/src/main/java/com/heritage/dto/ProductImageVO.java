package com.heritage.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProductImageVO {

    private Long id;

    private Long productId;

    private String imageUrl;

    private Integer sortOrder;

    private Integer isCover;

    private LocalDateTime createTime;
}
