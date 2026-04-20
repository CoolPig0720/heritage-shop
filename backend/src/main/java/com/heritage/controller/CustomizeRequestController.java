package com.heritage.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heritage.common.Result;
import com.heritage.dto.*;
import com.heritage.security.CustomUserDetails;
import com.heritage.service.CustomizeMessageService;
import com.heritage.service.CustomizeRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customize")
@RequiredArgsConstructor
@Tag(name = "定制渠道", description = "定制渠道相关接口")
public class CustomizeRequestController {

    private final CustomizeRequestService customizeRequestService;
    private final CustomizeMessageService customizeMessageService;

    // ====== 商家列表 ======

    @GetMapping("/merchants")
    @Operation(summary = "可选商家列表（分页）")
    public Result<Page<MerchantCardVO>> listMerchants(PageQuery query) {
        return Result.success(customizeRequestService.pageMerchants(query));
    }

    @GetMapping("/merchants/{merchantId}/products")
    @Operation(summary = "商家在售商品列表（分页）")
    public Result<Page<ProductVO>> pageMerchantProducts(@PathVariable Long merchantId, PageQuery query) {
        return Result.success(customizeRequestService.pageMerchantProducts(merchantId, query));
    }

    // ====== 定制工单 ======

    @GetMapping("/unread-count")
    @Operation(summary = "未读计数")
    public Result<CustomizeUnreadVO> getUnreadCount() {
        Long userId = getCurrentUserId();
        String role = getCurrentUserRole();
        return Result.success(customizeRequestService.getUnreadCount(userId, role));
    }

    @PostMapping("/requests")
    @Operation(summary = "发起定制请求")
    public Result<Long> createRequest(@Valid @RequestBody CustomizeRequestCreateRequest request) {
        Long userId = getCurrentUserId();
        return Result.success(customizeRequestService.createRequest(userId, request));
    }

    @GetMapping("/requests/mine")
    @Operation(summary = "我的定制列表")
    public Result<Page<CustomizeRequestVO>> pageMyRequests(PageQuery query,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        Long userId = getCurrentUserId();
        return Result.success(customizeRequestService.pageMyRequests(userId, query, status, keyword));
    }

    @GetMapping("/requests/received")
    @Operation(summary = "收到的定制列表")
    public Result<Page<CustomizeRequestVO>> pageReceivedRequests(PageQuery query,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        Long merchantId = getCurrentUserId();
        return Result.success(customizeRequestService.pageReceivedRequests(merchantId, query, status, keyword));
    }

    @GetMapping("/requests/{id}")
    @Operation(summary = "工单详情")
    public Result<CustomizeRequestVO> getDetail(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        return Result.success(customizeRequestService.getDetail(userId, id));
    }

    @PutMapping("/requests/{id}/quote")
    @Operation(summary = "商家报价")
    public Result<Void> quote(@PathVariable Long id, @Valid @RequestBody CustomizeQuoteRequest request) {
        Long merchantId = getCurrentUserId();
        customizeRequestService.quote(merchantId, id, request);
        return Result.success();
    }

    @PutMapping("/requests/{id}/confirm")
    @Operation(summary = "用户确认报价")
    public Result<Long> confirm(@PathVariable Long id, @Valid @RequestBody CustomizeConfirmRequest request) {
        Long userId = getCurrentUserId();
        return Result.success(customizeRequestService.confirm(userId, id, request));
    }

    @PutMapping("/requests/{id}/status")
    @Operation(summary = "更新工单状态")
    public Result<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody CustomizeStatusUpdateRequest request) {
        Long userId = getCurrentUserId();
        String role = getCurrentUserRole();
        customizeRequestService.updateStatus(userId, role, id, request);
        return Result.success();
    }

    // ====== 沟通消息 ======

    @PostMapping("/requests/{requestId}/messages")
    @Operation(summary = "发送消息")
    public Result<Long> sendMessage(@PathVariable Long requestId,
            @Valid @RequestBody CustomizeMessageCreateRequest request) {
        Long userId = getCurrentUserId();
        String role = getCurrentUserRole();
        return Result.success(customizeMessageService.sendMessage(userId, role, requestId, request));
    }

    @GetMapping("/requests/{requestId}/messages")
    @Operation(summary = "消息列表")
    public Result<List<CustomizeMessageVO>> listMessages(@PathVariable Long requestId) {
        Long userId = getCurrentUserId();
        return Result.success(customizeMessageService.listMessages(userId, requestId));
    }

    @PutMapping("/requests/{requestId}/messages/read")
    @Operation(summary = "标记消息已读")
    public Result<Void> markMessagesRead(@PathVariable Long requestId) {
        Long userId = getCurrentUserId();
        customizeMessageService.markAsRead(userId, requestId);
        return Result.success();
    }

    // ====== 辅助方法 ======

    private Long getCurrentUserId() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        return userDetails.getUserId();
    }

    private String getCurrentUserRole() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        return userDetails.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("USER");
    }
}
