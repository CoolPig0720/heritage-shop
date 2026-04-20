package com.heritage.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class HeritageStoryCreateRequest {

    @NotBlank(message = "故事标题不能为空")
    private String title;

    @NotBlank(message = "故事内容不能为空")
    private String content;

    private Long heritageProjectId;

    private Integer status;

    private Integer isTop;
}
