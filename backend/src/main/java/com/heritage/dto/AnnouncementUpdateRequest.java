package com.heritage.dto;

import lombok.Data;

@Data
public class AnnouncementUpdateRequest {

    private String title;
    private String content;
    private Integer status;
    private Integer isTop;
}
