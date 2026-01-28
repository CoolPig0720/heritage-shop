package com.heritage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "账号不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_]{4,20}$", message = "账号必须是4-20位字母、数字或下划线")
    private String account;

    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,20}$", message = "密码必须是8-20位，至少包含字母和数字")
    private String password;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    @NotBlank(message = "名称不能为空")
    @Size(min = 2, max = 20, message = "名称长度必须在2-20位之间")
    private String name;

    @NotBlank(message = "角色不能为空")
    private String role;
}
