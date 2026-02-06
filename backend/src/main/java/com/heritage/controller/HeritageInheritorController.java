package com.heritage.controller;

import com.heritage.common.Result;
import com.heritage.dto.HeritageInheritorDetailVO;
import com.heritage.service.HeritageInheritorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/heritage/inheritors")
@RequiredArgsConstructor
@Tag(name = "非遗传承人", description = "非遗传承人相关接口")
public class HeritageInheritorController {

    private final HeritageInheritorService heritageInheritorService;

    @GetMapping("/{id}")
    @Operation(summary = "传承人详情")
    public Result<HeritageInheritorDetailVO> detail(@PathVariable Long id) {
        return Result.success(heritageInheritorService.getInheritorDetail(id));
    }
}

