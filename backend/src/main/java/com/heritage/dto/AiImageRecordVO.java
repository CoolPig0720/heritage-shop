package com.heritage.dto;

import lombok.Data;

@Data
public class AiImageRecordVO {

    private Long id;

    private String originalImageUrl;

    private String prompt;

    private String resultImageUrl;
}

