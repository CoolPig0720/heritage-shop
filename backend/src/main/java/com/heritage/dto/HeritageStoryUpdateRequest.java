package com.heritage.dto;

import lombok.Data;

@Data
public class HeritageStoryUpdateRequest {

    private String title;
    private String content;
    private Long heritageProjectId;
    private Integer status;
    private Integer isTop;
}
