package com.heritage.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentVO {

    private Long id;

    private Long orderId;

    private String payNo;

    private BigDecimal amount;

    private String channel;

    private String status;

    private String payUrl;

    private LocalDateTime createTime;

    private LocalDateTime paidTime;

    private String failReason;
}
