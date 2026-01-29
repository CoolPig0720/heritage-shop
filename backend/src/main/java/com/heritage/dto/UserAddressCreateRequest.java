package com.heritage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserAddressCreateRequest {

    @NotBlank(message = "收货人姓名不能为空")
    @Size(max = 50, message = "收货人姓名长度不能超过50")
    private String receiverName;

    @NotBlank(message = "收货人手机号不能为空")
    @Size(max = 20, message = "收货人手机号长度不能超过20")
    private String receiverPhone;

    @NotBlank(message = "地区不能为空")
    @Size(max = 512, message = "地区长度不能超过512")
    private String regionNamePath;

    @Size(max = 512, message = "地区编码长度不能超过512")
    private String regionCodePath;

    private Integer regionDepth;

    @Size(max = 50, message = "省长度不能超过50")
    private String province;

    @Size(max = 50, message = "市长度不能超过50")
    private String city;

    @Size(max = 50, message = "区长度不能超过50")
    private String district;

    @NotBlank(message = "详细地址不能为空")
    @Size(max = 255, message = "详细地址长度不能超过255")
    private String detailAddress;

    private Boolean isDefault;
}
