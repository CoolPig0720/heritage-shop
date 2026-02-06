package com.heritage.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heritage.common.Result;
import com.heritage.dto.HeritageProjectCreateRequest;
import com.heritage.dto.HeritageProjectDetailVO;
import com.heritage.dto.HeritageProjectInheritorsUpdateRequest;
import com.heritage.dto.HeritageProjectListItemVO;
import com.heritage.dto.HeritageProjectMediaCreateRequest;
import com.heritage.dto.HeritageProjectMediaUpdateRequest;
import com.heritage.dto.HeritageProjectUpdateRequest;
import com.heritage.dto.PageQuery;
import com.heritage.service.HeritageProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/heritage/projects")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "非遗管理-项目", description = "管理端非遗项目接口（管理员）")
public class HeritageAdminProjectController {

    private final HeritageProjectService heritageProjectService;

    @GetMapping
    @Operation(summary = "项目分页（管理员）")
    public Result<Page<HeritageProjectListItemVO>> page(PageQuery query, @RequestParam(required = false) Long categoryId) {
        return Result.success(heritageProjectService.pageProjects(query, categoryId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "项目详情（管理员）")
    public Result<HeritageProjectDetailVO> detail(@PathVariable Long id) {
        return Result.success(heritageProjectService.getProjectDetail(id));
    }

    @PostMapping
    @Operation(summary = "新增项目（管理员）")
    public Result<Long> create(@Valid @RequestBody HeritageProjectCreateRequest request) {
        return Result.success(heritageProjectService.createProject(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新项目（管理员）")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody HeritageProjectUpdateRequest request) {
        heritageProjectService.updateProject(id, request);
        return Result.success();
    }

    @PutMapping("/{id}/inheritors")
    @Operation(summary = "更新项目传承人（管理员）")
    public Result<Void> updateInheritors(@PathVariable Long id, @Valid @RequestBody HeritageProjectInheritorsUpdateRequest request) {
        heritageProjectService.updateProjectInheritors(id, request.getInheritorIds());
        return Result.success();
    }

    @PostMapping("/{id}/medias")
    @Operation(summary = "新增项目媒体（管理员）")
    public Result<Long> addMedia(@PathVariable Long id, @Valid @RequestBody HeritageProjectMediaCreateRequest request) {
        return Result.success(heritageProjectService.addProjectMedia(id, request));
    }

    @PutMapping("/medias/{mediaId}")
    @Operation(summary = "更新项目媒体（管理员）")
    public Result<Void> updateMedia(@PathVariable Long mediaId, @RequestBody HeritageProjectMediaUpdateRequest request) {
        heritageProjectService.updateProjectMedia(mediaId, request);
        return Result.success();
    }

    @DeleteMapping("/medias/{mediaId}")
    @Operation(summary = "删除项目媒体（管理员）")
    public Result<Void> deleteMedia(@PathVariable Long mediaId) {
        heritageProjectService.deleteProjectMedia(mediaId);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除项目（管理员）")
    public Result<Void> delete(@PathVariable Long id) {
        heritageProjectService.deleteProject(id);
        return Result.success();
    }
}
