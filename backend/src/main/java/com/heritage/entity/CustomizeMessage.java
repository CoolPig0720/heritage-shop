package com.heritage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("customize_message")
public class CustomizeMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("request_id")
    private Long requestId;

    @TableField("sender_id")
    private Long senderId;

    @TableField("sender_type")
    private String senderType;

    @TableField("content")
    private String content;

    @TableField("is_read")
    private Integer isRead;

    @TableField("read_time")
    private LocalDateTime readTime;

    @TableField("create_time")
    private LocalDateTime createTime;
}
