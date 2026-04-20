package com.heritage.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CustomizeRequestVO {

    private Long id;

    private Long userId;

    private Long merchantId;

    private String title;

    private String description;

    private List<String> imageUrls;

    private BigDecimal quotedPrice;

    private Long addressId;

    private String status;

    private Long orderId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String userName;

    private String merchantName;

    private String orderNo;

    private Long unreadCount;
}
