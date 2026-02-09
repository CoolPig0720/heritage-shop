package com.heritage.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiTextToImageRequest {

    @NotBlank(message = "Prompt不能为空")
    private String prompt;

    private String resolution;

    private Long seed;

    private String style;

    private String rspImgType;
}

