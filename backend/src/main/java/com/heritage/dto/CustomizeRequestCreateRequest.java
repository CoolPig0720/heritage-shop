package com.heritage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CustomizeRequestCreateRequest {

    @NotNull(message = "商家ID不能为空")
    private Long merchantId;

    @NotBlank(message = "定制标题不能为空")
    @Size(max = 200, message = "定制标题不超过200字符")
    private String title;

    @NotBlank(message = "需求描述不能为空")
    @Size(max = 2000, message = "需求描述不超过2000字符")
    private String description;

    @Size(max = 3, message = "参考图片最多3张")
    private List<String> imageUrls;
}
