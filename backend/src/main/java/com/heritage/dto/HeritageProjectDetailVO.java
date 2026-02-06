package com.heritage.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class HeritageProjectDetailVO {

    private Long id;

    private Long categoryId;

    private String categoryName;

    private String name;

    private String description;

    private String applyUnit;

    private String protectUnit;

    private List<HeritageProjectMediaVO> medias = new ArrayList<>();

    private List<HeritageInheritorSimpleVO> inheritors = new ArrayList<>();
}

