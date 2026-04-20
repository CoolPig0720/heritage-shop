package com.heritage.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heritage.common.Result;
import com.heritage.dto.AnnouncementCreateRequest;
import com.heritage.dto.AnnouncementUpdateRequest;
import com.heritage.dto.AnnouncementVO;
import com.heritage.dto.PageQuery;
import com.heritage.service.AnnouncementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/announcements")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "资讯管理-公告", description = "管理端公告接口（管理员）")
public class AnnouncementAdminController {

    private final AnnouncementService announcementService;

    @PostMapping
    @Operation(summary = "新增公告（管理员）")
    public Result<Long> create(@Valid @RequestBody AnnouncementCreateRequest request) {
        return Result.success(announcementService.createAnnouncement(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改公告（管理员）")
    public Result<Void> update(@PathVariable Long id, @RequestBody AnnouncementUpdateRequest request) {
        announcementService.updateAnnouncement(id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除公告（管理员）")
    public Result<Void> delete(@PathVariable Long id) {
        announcementService.deleteAnnouncement(id);
        return Result.success();
    }

    @GetMapping
    @Operation(summary = "公告管理分页列表（管理员）")
    public Result<Page<AnnouncementVO>> page(PageQuery query, @RequestParam(required = false) Integer status) {
        return Result.success(announcementService.getManagePage(query, status));
    }

    @GetMapping("/{id}")
    @Operation(summary = "公告详情（管理员）")
    public Result<AnnouncementVO> detail(@PathVariable Long id) {
        return Result.success(announcementService.getDetail(id));
    }
}
