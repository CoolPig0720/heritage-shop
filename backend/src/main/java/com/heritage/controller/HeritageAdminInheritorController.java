package com.heritage.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heritage.common.Result;
import com.heritage.dto.HeritageInheritorAdminVO;
import com.heritage.dto.HeritageInheritorCreateRequest;
import com.heritage.dto.HeritageInheritorUpdateRequest;
import com.heritage.dto.PageQuery;
import com.heritage.service.HeritageInheritorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/heritage/inheritors")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "非遗管理-传承人", description = "管理端非遗传承人接口（管理员）")
public class HeritageAdminInheritorController {

    private final HeritageInheritorService heritageInheritorService;

    @GetMapping
    @Operation(summary = "传承人分页（管理员）")
    public Result<Page<HeritageInheritorAdminVO>> page(PageQuery query) {
        return Result.success(heritageInheritorService.pageAdminInheritors(query));
    }

    @GetMapping("/{id}")
    @Operation(summary = "传承人详情（管理员）")
    public Result<HeritageInheritorAdminVO> detail(@PathVariable Long id) {
        return Result.success(heritageInheritorService.getAdminInheritor(id));
    }

    @PostMapping
    @Operation(summary = "新增传承人（管理员）")
    public Result<Long> create(@Valid @RequestBody HeritageInheritorCreateRequest request) {
        return Result.success(heritageInheritorService.createInheritor(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新传承人（管理员）")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody HeritageInheritorUpdateRequest request) {
        heritageInheritorService.updateInheritor(id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除传承人（管理员）")
    public Result<Void> delete(@PathVariable Long id) {
        heritageInheritorService.deleteInheritor(id);
        return Result.success();
    }
}
