package com.heritage.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heritage.common.Result;
import com.heritage.dto.AnnouncementVO;
import com.heritage.dto.HeritageStoryVO;
import com.heritage.dto.PageQuery;
import com.heritage.service.AnnouncementService;
import com.heritage.service.HeritageStoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/info")
@RequiredArgsConstructor
@Tag(name = "前台资讯", description = "前台公告与非遗故事展示接口")
public class InfoController {

    private final AnnouncementService announcementService;
    private final HeritageStoryService heritageStoryService;

    @GetMapping("/announcements")
    @Operation(summary = "公告列表（所有登录用户）")
    public Result<Page<AnnouncementVO>> announcementPage(PageQuery query) {
        return Result.success(announcementService.getPublishedPage(query));
    }

    @GetMapping("/announcements/{id}")
    @Operation(summary = "公告详情（所有登录用户）")
    public Result<AnnouncementVO> announcementDetail(@PathVariable Long id) {
        return Result.success(announcementService.getDetail(id));
    }

    @GetMapping("/heritage-stories")
    @Operation(summary = "非遗故事列表（所有登录用户）")
    public Result<Page<HeritageStoryVO>> heritageStoryPage(PageQuery query) {
        return Result.success(heritageStoryService.getPublishedPage(query));
    }

    @GetMapping("/heritage-stories/{id}")
    @Operation(summary = "非遗故事详情（所有登录用户）")
    public Result<HeritageStoryVO> heritageStoryDetail(@PathVariable Long id) {
        return Result.success(heritageStoryService.getDetail(id));
    }
}
