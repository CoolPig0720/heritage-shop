package com.heritage.controller;

import com.heritage.common.Result;
import com.heritage.dto.HeritageCategoryTreeVO;
import com.heritage.service.HeritageCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/heritage/categories")
@RequiredArgsConstructor
@Tag(name = "非遗分类", description = "非遗分类相关接口")
public class HeritageCategoryController {

    private final HeritageCategoryService heritageCategoryService;

    @GetMapping("/tree")
    @Operation(summary = "分类树")
    public Result<List<HeritageCategoryTreeVO>> tree() {
        return Result.success(heritageCategoryService.getCategoryTree());
    }
}

