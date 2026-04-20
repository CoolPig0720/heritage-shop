package com.heritage.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HeritageStoryVO {

    private Long id;
    private String title;
    private String content;
    private Long heritageProjectId;
    private String heritageProjectName;
    private Integer status;
    private Integer isTop;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
