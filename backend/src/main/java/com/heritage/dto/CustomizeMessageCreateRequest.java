package com.heritage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CustomizeMessageCreateRequest {

    @NotBlank(message = "消息内容不能为空")
    @Size(max = 500, message = "消息内容不超过500字符")
    private String content;
}
