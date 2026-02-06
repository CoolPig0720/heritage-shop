package com.heritage.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class HeritageCategoryTreeVO {

    private Long id;

    private String name;

    private Long parentId;

    private Integer sortOrder;

    private List<HeritageCategoryTreeVO> children = new ArrayList<>();
}

