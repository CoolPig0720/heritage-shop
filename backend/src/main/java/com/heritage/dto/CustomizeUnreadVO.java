package com.heritage.dto;

import lombok.Data;

@Data
public class CustomizeUnreadVO {

    private Long pendingCount;

    private Long quotedCount;

    private Long confirmedCount;

    private Long cancelledCount;

    private Long completedCount;

    private Long unreadMessageCount;
}
