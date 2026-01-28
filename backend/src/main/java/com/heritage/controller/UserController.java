package com.heritage.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heritage.common.Result;
import com.heritage.dto.PasswordUpdateRequest;
import com.heritage.dto.UserUpdateRequest;
import com.heritage.dto.UserVO;
import com.heritage.dto.PageQuery;
import com.heritage.service.UserService;
import com.heritage.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户信息管理相关接口")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    @Operation(summary = "获取当前用户信息")
    public Result<UserVO> getProfile(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getUserId();
        UserVO userVO = userService.getUserInfo(userId);
        return Result.success(userVO);
    }

    @PutMapping("/profile")
    @Operation(summary = "更新当前用户信息")
    public Result<Void> updateProfile(Authentication authentication,
                                       @Valid @RequestBody UserUpdateRequest request) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getUserId();
        userService.updateUserInfo(userId, request);
        return Result.success();
    }

    @PutMapping("/password")
    @Operation(summary = "修改密码")
    public Result<Void> updatePassword(Authentication authentication,
                                         @Valid @RequestBody PasswordUpdateRequest request) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getUserId();
        userService.updatePassword(userId, request);
        return Result.success();
    }

    @GetMapping("/list")
    @Operation(summary = "获取用户列表（管理员）")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<UserVO>> getUserList(PageQuery query) {
        Page<UserVO> page = userService.getUserList(query);
        return Result.success(page);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户（管理员）")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.removeById(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "修改用户状态（管理员）")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateUserStatus(@PathVariable Long id,
                                        @RequestBody UserUpdateRequest request) {
        userService.updateUserInfo(id, request);
        return Result.success();
    }

    @PutMapping("/{id}/update")
    @Operation(summary = "更新用户信息（管理员）")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateUser(@PathVariable Long id,
                                   @RequestBody UserUpdateRequest request) {
        userService.updateUserInfo(id, request);
        return Result.success();
    }
}
