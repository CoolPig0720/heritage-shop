package com.heritage.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class HeritageInheritorAdminVO {

    private Long id;

    private String name;

    private String gender;

    private LocalDate birthDate;

    private String nation;

    private String photo;

    private String description;
}
