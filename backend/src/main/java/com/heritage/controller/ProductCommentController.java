package com.heritage.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heritage.common.Result;
import com.heritage.dto.CommentCreateRequest;
import com.heritage.dto.CommentLikeVO;
import com.heritage.dto.CommentVO;
import com.heritage.security.CustomUserDetails;
import com.heritage.service.ProductCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "商品评论", description = "商品评论相关接口")
public class ProductCommentController {

    private final ProductCommentService commentService;

    // ==================== 公开接口（无需登录） ====================

    @GetMapping("/api/shop/products/{productId}/comments")
    @Operation(summary = "获取商品顶级评论列表")
    public Result<Page<CommentVO>> listProductComments(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long currentUserId = getCurrentUserId();
        return Result.success(commentService.listProductComments(productId, currentUserId, page, size));
    }

    @GetMapping("/api/shop/comments/{commentId}/replies")
    @Operation(summary = "获取评论的回复列表")
    public Result<Page<CommentVO>> listCommentReplies(
            @PathVariable Long commentId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long currentUserId = getCurrentUserId();
        return Result.success(commentService.listCommentReplies(commentId, currentUserId, page, size));
    }

    // ==================== 需要登录的接口 ====================

    @PostMapping("/api/comments")
    @Operation(summary = "发表评论或回复")
    public Result<Long> createComment(@Valid @RequestBody CommentCreateRequest request) {
        Long userId = getRequiredUserId();
        Long commentId = commentService.createComment(userId, request);
        return Result.success(commentId);
    }

    @DeleteMapping("/api/comments/{id}")
    @Operation(summary = "删除评论")
    public Result<Void> deleteComment(@PathVariable Long id) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        Long userId = userDetails.getUserId();
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        commentService.deleteComment(userId, isAdmin, id);
        return Result.success();
    }

    @PostMapping("/api/comments/{id}/like")
    @Operation(summary = "点赞/取消点赞")
    public Result<CommentLikeVO> toggleLike(@PathVariable Long id) {
        Long userId = getRequiredUserId();
        return Result.success(commentService.toggleLike(userId, id));
    }

    // ==================== 工具方法 ====================

    /**
     * 获取当前登录用户ID，未登录返回null
     */
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

    /**
     * 获取当前登录用户ID，未登录抛异常
     */
    private Long getRequiredUserId() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return userDetails.getUserId();
    }
}
