package com.heritage.dto;

import lombok.Data;

import java.util.List;

@Data
public class MerchantCardVO {

    private Long id;

    private String name;

    private String avatar;

    private Integer productCount;

    private List<MerchantProductBriefVO> topProducts;
}
