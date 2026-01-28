package com.heritage.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVO {
    private Long id;
    private String account;
    private String name;
    private String role;
    private String avatar;
    private String certificateNumber;
    private String password;
    private LocalDateTime registerTime;
}
