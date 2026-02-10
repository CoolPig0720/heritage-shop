package com.heritage.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderVO {

    private Long id;

    private String orderNo;

    private BigDecimal totalAmount;

    private String status;

    private Long addressId;

    private String receiverName;

    private String receiverPhone;

    private String regionNamePath;

    private String detailAddress;

    private LocalDateTime createTime;

    private LocalDateTime paidTime;

    private LocalDateTime cancelTime;

    private List<OrderItemVO> items;
}
