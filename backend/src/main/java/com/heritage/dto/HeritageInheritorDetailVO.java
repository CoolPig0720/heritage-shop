package com.heritage.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class HeritageInheritorDetailVO {

    private Long id;

    private String name;

    private String gender;

    private LocalDate birthDate;

    private String nation;

    private String photo;

    private String description;

    private List<HeritageProjectListItemVO> projects = new ArrayList<>();
}

