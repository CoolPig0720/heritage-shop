package com.heritage.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class HeritageInheritorSimpleVO {

    private Long id;

    private String name;

    private String gender;

    private LocalDate birthDate;

    private String nation;

    private String photo;
}

