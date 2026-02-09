package com.heritage.dto;

import lombok.Data;

import java.util.List;

@Data
public class AiImageGenerateResponse {

    private String requestId;

    private String resultImage;

    private List<String> resultImages;

    private Long seed;

    private List<Long> seeds;
}
