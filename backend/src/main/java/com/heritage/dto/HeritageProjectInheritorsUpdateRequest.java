package com.heritage.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class HeritageProjectInheritorsUpdateRequest {

    @NotNull(message = "传承人ID列表不能为空")
    private List<Long> inheritorIds = new ArrayList<>();
}
