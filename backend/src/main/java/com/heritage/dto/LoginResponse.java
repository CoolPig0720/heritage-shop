package com.heritage.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private UserVO userInfo;
}
