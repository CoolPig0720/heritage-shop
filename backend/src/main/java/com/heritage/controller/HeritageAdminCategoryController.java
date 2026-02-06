package com.heritage.controller;

import com.heritage.common.Result;
import com.heritage.dto.HeritageCategoryCreateRequest;
import com.heritage.dto.HeritageCategoryTreeVO;
import com.heritage.service.HeritageCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@RestController
@RequestMapping("/api/admin/heritage/categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "非遗管理-分类", description = "管理端非遗分类接口（管理员）")
public class HeritageAdminCategoryController {

    private final HeritageCategoryService heritageCategoryService;

    @GetMapping("/tree")
    @Operation(summary = "分类树（管理员）")
    public Result<List<HeritageCategoryTreeVO>> tree() {
        return Result.success(heritageCategoryService.getCategoryTree());
    }

    @PostMapping
    @Operation(summary = "新增类型（管理员）")
    public Result<Long> create(@Valid @RequestBody HeritageCategoryCreateRequest request) {
        return Result.success(heritageCategoryService.createCategory(request));
    }
}
