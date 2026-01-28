package com.heritage.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductVO {

    private Long id;

    private String name;

    private String description;

    private BigDecimal price;

    private Long merchantId;

    private String merchantName;

    private String traceCode;

    private String traceQrUrl;

    private String model3dUrl;

    private Integer status;

    private String coverImageUrl;

    private List<String> imageUrls;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
