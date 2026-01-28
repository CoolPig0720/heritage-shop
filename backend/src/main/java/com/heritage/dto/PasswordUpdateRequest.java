package com.heritage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PasswordUpdateRequest {

    @NotBlank(message = "原密码不能为空")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,20}$", message = "密码必须是8-20位，至少包含字母和数字")
    private String newPassword;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
}
