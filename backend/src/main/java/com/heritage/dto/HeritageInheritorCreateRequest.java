package com.heritage.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class HeritageInheritorCreateRequest {

    @NotBlank(message = "姓名不能为空")
    private String name;

    private String gender;

    private LocalDate birthDate;

    private String nation;

    private String photo;

    private String description;
}
