package com.heritage.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class HeritageProjectListItemVO {

    private Long id;

    private Long categoryId;

    private String categoryName;

    private String name;

    private String applyUnit;

    private String protectUnit;

    private String inheritorNames;

    private List<HeritageInheritorSimpleVO> inheritors = new ArrayList<>();
}
