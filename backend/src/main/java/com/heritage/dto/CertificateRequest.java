package com.heritage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CertificateRequest {

    @NotBlank(message = "证件号不能为空")
    private String certificateNumber;

    @NotBlank(message = "真实姓名不能为空")
    @Size(min = 2, max = 20, message = "姓名长度必须在2-20位之间")
    private String name;
}
