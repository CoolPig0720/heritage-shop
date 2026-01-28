package com.heritage.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class ProductImageAddRequest {

    @NotEmpty(message = "图片列表不能为空")
    private List<@NotBlank(message = "图片地址不能为空") String> imageUrls;

    private Boolean setFirstAsCover;
}
