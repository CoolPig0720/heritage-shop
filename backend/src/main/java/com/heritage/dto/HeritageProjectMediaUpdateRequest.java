package com.heritage.dto;

import lombok.Data;

@Data
public class HeritageProjectMediaUpdateRequest {

    private String mediaType;

    private String mediaUrl;

    private Integer sortOrder;
}
