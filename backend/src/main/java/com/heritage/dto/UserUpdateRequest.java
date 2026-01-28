package com.heritage.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequest {

    @Size(min = 2, max = 20, message = "名称长度必须在2-20位之间")
    private String name;

    private String avatar;

    private String password;
}
