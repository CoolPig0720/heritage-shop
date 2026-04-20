package com.heritage.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AnnouncementVO {

    private Long id;
    private String title;
    private String content;
    private Integer status;
    private Integer isTop;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
