package com.heritage.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserAddressVO {
    private Long id;
    private Long userId;
    private String receiverName;
    private String receiverPhone;
    private String regionCodePath;
    private String regionNamePath;
    private Integer regionDepth;
    private String province;
    private String city;
    private String district;
    private String detailAddress;
    private Integer isDefault;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
