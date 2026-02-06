package com.heritage.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heritage.common.Result;
import com.heritage.dto.HeritageProjectDetailVO;
import com.heritage.dto.HeritageProjectListItemVO;
import com.heritage.dto.PageQuery;
import com.heritage.service.HeritageProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/heritage/projects")
@RequiredArgsConstructor
@Tag(name = "非遗项目", description = "非遗项目相关接口")
public class HeritageProjectController {

    private final HeritageProjectService heritageProjectService;

    @GetMapping
    @Operation(summary = "项目分页列表")
    public Result<Page<HeritageProjectListItemVO>> page(PageQuery query,
                                                        @RequestParam(required = false) Long categoryId) {
        return Result.success(heritageProjectService.pageProjects(query, categoryId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "项目详情")
    public Result<HeritageProjectDetailVO> detail(@PathVariable Long id) {
        return Result.success(heritageProjectService.getProjectDetail(id));
    }
}

