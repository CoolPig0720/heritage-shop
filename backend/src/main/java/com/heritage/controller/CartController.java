package com.heritage.controller;

import com.heritage.common.Result;
import com.heritage.dto.CartItemAddRequest;
import com.heritage.dto.CartItemQuantityUpdateRequest;
import com.heritage.dto.CartItemSelectedUpdateRequest;
import com.heritage.dto.CartItemVO;
import com.heritage.dto.CartSummaryVO;
import com.heritage.security.CustomUserDetails;
import com.heritage.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "购物车", description = "购物车相关接口")
public class CartController {

    private final CartService cartService;

    @PostMapping("/items")
    @Operation(summary = "加入购物车")
    public Result<Long> addItem(@Valid @RequestBody CartItemAddRequest request) {
        Long userId = getCurrentUserId();
        return Result.success(cartService.addItem(userId, request));
    }

    @GetMapping("/items")
    @Operation(summary = "购物车列表")
    public Result<List<CartItemVO>> listItems() {
        Long userId = getCurrentUserId();
        return Result.success(cartService.listItems(userId));
    }

    @PutMapping("/items/{id}/quantity")
    @Operation(summary = "修改数量")
    public Result<Void> updateQuantity(@PathVariable Long id, @Valid @RequestBody CartItemQuantityUpdateRequest request) {
        Long userId = getCurrentUserId();
        cartService.updateQuantity(userId, id, request);
        return Result.success();
    }

    @PutMapping("/items/{id}/selected")
    @Operation(summary = "勾选/取消勾选")
    public Result<Void> updateSelected(@PathVariable Long id, @Valid @RequestBody CartItemSelectedUpdateRequest request) {
        Long userId = getCurrentUserId();
        cartService.updateSelected(userId, id, request);
        return Result.success();
    }

    @DeleteMapping("/items/{id}")
    @Operation(summary = "删除条目")
    public Result<Void> deleteItem(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        cartService.deleteItem(userId, id);
        return Result.success();
    }

    @GetMapping("/summary")
    @Operation(summary = "汇总信息")
    public Result<CartSummaryVO> summary() {
        Long userId = getCurrentUserId();
        return Result.success(cartService.getSummary(userId));
    }

    private Long getCurrentUserId() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUserId();
    }
}

