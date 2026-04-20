package com.heritage.controller;

import com.heritage.common.Result;
import com.heritage.dto.ProductRatingRequest;
import com.heritage.dto.ProductRatingVO;
import com.heritage.security.CustomUserDetails;
import com.heritage.service.ProductRatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "商品评分", description = "商品评分相关接口")
public class ProductRatingController {

    private final ProductRatingService ratingService;

    // ==================== 公开接口（无需登录） ====================

    @GetMapping("/api/shop/products/{productId}/rating")
    @Operation(summary = "获取商品综合评分")
    public Result<ProductRatingVO> getProductRating(@PathVariable Long productId) {
        Long currentUserId = getCurrentUserId();
        return Result.success(ratingService.getProductRating(productId, currentUserId));
    }

    // ==================== 需要登录的接口 ====================

    @PostMapping("/api/ratings")
    @Operation(summary = "提交/更新评分")
    public Result<Void> submitRating(@Valid @RequestBody ProductRatingRequest request) {
        Long userId = getRequiredUserId();
        ratingService.submitRating(userId, request);
        return Result.success();
    }

    @GetMapping("/api/ratings/my")
    @Operation(summary = "获取当前用户对某商品的评分")
    public Result<Integer> getMyRating(@RequestParam Long productId) {
        Long userId = getRequiredUserId();
        return Result.success(ratingService.getMyRating(userId, productId));
    }

    // ==================== 工具方法 ====================

    private Long getCurrentUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
                return ((CustomUserDetails) auth.getPrincipal()).getUserId();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private Long getRequiredUserId() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return userDetails.getUserId();
    }
}
