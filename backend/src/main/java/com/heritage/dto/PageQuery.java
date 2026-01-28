package com.heritage.dto;

import lombok.Data;

@Data
public class PageQuery {

    private Integer page = 1;

    private Integer size = 10;

    private String keyword;

    private String name;

    private String role;

    private String sortField;

    private String sortOrder;
}
