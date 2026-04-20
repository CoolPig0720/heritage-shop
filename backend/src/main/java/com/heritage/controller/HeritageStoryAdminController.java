package com.heritage.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heritage.common.Result;
import com.heritage.dto.HeritageStoryCreateRequest;
import com.heritage.dto.HeritageStoryUpdateRequest;
import com.heritage.dto.HeritageStoryVO;
import com.heritage.dto.PageQuery;
import com.heritage.service.HeritageStoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/heritage-stories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "资讯管理-非遗故事", description = "管理端非遗故事接口（管理员）")
public class HeritageStoryAdminController {

    private final HeritageStoryService heritageStoryService;

    @PostMapping
    @Operation(summary = "新增非遗故事（管理员）")
    public Result<Long> create(@Valid @RequestBody HeritageStoryCreateRequest request) {
        return Result.success(heritageStoryService.createHeritageStory(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改非遗故事（管理员）")
    public Result<Void> update(@PathVariable Long id, @RequestBody HeritageStoryUpdateRequest request) {
        heritageStoryService.updateHeritageStory(id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除非遗故事（管理员）")
    public Result<Void> delete(@PathVariable Long id) {
        heritageStoryService.deleteHeritageStory(id);
        return Result.success();
    }

    @GetMapping
    @Operation(summary = "非遗故事管理分页列表（管理员）")
    public Result<Page<HeritageStoryVO>> page(PageQuery query,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long heritageProjectId) {
        return Result.success(heritageStoryService.getManagePage(query, status, heritageProjectId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "非遗故事详情（管理员）")
    public Result<HeritageStoryVO> detail(@PathVariable Long id) {
        return Result.success(heritageStoryService.getDetail(id));
    }
}
