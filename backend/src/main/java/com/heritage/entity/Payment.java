package com.heritage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("payment")
public class Payment {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("pay_no")
    private String payNo;

    @TableField("order_id")
    private Long orderId;

    @TableField("amount")
    private BigDecimal amount;

    @TableField("channel")
    private String channel;

    @TableField("status")
    private String status;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("paid_time")
    private LocalDateTime paidTime;

    @TableField("fail_reason")
    private String failReason;
}
