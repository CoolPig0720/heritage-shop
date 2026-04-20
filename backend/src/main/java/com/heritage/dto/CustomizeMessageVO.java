package com.heritage.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CustomizeMessageVO {

    private Long id;

    private Long requestId;

    private Long senderId;

    private String senderType;

    private String content;

    private LocalDateTime createTime;

    private String senderName;

    private String senderAvatar;

    private Integer isRead;
}
