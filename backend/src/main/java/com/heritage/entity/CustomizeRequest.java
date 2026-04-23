package com.heritage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "customize_request", autoResultMap = true)
public class CustomizeRequest {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("merchant_id")
    private Long merchantId;

    @TableField("title")
    private String title;

    @TableField("description")
    private String description;

    @TableField(value = "image_urls", typeHandler = JacksonTypeHandler.class)
    private List<String> imageUrls;

    @TableField("quoted_price")
    private BigDecimal quotedPrice;

    @TableField("address_id")
    private Long addressId;

    @TableField("status")
    private String status;

    @TableField("order_id")
    private Long orderId;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 用户查看已完成状态的时间（用于红点消除）
     */
    @TableField("user_completed_read_at")
    private LocalDateTime userCompletedReadAt;

    /**
     * 商家查看已取消状态的时间（用于红点消除）
     */
    @TableField("merchant_cancelled_read_at")
    private LocalDateTime merchantCancelledReadAt;
}
