package com.heritage.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heritage.common.Result;
import com.heritage.dto.OrderCreateRequest;
import com.heritage.dto.OrderVO;
import com.heritage.dto.PageQuery;
import com.heritage.security.CustomUserDetails;
import com.heritage.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "订单", description = "订单相关接口")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "从购物车创建订单")
    public Result<Long> create(@Valid @RequestBody OrderCreateRequest request) {
        Long userId = getCurrentUserId();
        return Result.success(orderService.createOrderFromCart(userId, request));
    }

    @GetMapping
    @Operation(summary = "订单分页列表")
    public Result<Page<OrderVO>> page(PageQuery query, @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        Long userId = getCurrentUserId();
        return Result.success(orderService.pageMyOrders(userId, query, status, keyword));
    }

    @GetMapping("/{id}")
    @Operation(summary = "订单详情")
    public Result<OrderVO> detail(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        return Result.success(orderService.getMyOrderDetail(userId, id));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "取消订单")
    public Result<Void> cancel(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        orderService.cancelMyOrder(userId, id);
        return Result.success();
    }

    private Long getCurrentUserId() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        return userDetails.getUserId();
    }
}
