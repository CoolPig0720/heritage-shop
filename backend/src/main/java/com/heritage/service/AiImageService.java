package com.heritage.service;

import com.heritage.dto.AiImageGenerateResponse;
import com.heritage.dto.AiTextToImageRequest;

public interface AiImageService {

    AiImageGenerateResponse textToImage(AiTextToImageRequest request);

    AiImageGenerateResponse imageToImage(byte[] imageBytes,
                                         String prompt,
                                         String negativePrompt,
                                         Float strength,
                                         String resolution,
                                         Boolean enhanceImage,
                                         Boolean restoreFace,
                                         String[] styles,
                                         Integer count,
                                         String rspImgType);
}
