package com.heritage.controller;

import com.heritage.common.Result;
import com.heritage.dto.UserAddressCreateRequest;
import com.heritage.dto.UserAddressUpdateRequest;
import com.heritage.dto.UserAddressVO;
import com.heritage.security.CustomUserDetails;
import com.heritage.service.UserAddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/address")
@RequiredArgsConstructor
@Tag(name = "收货地址", description = "收货地址管理相关接口")
public class UserAddressController {

    private final UserAddressService userAddressService;

    @PostMapping
    @Operation(summary = "新增地址")
    public Result<Long> create(@Valid @RequestBody UserAddressCreateRequest request) {
        Long userId = getCurrentUserId();
        Long id = userAddressService.createAddress(userId, request);
        return Result.success(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改地址")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody UserAddressUpdateRequest request) {
        Long userId = getCurrentUserId();
        userAddressService.updateAddress(userId, id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除地址")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        userAddressService.deleteAddress(userId, id);
        return Result.success();
    }

    @GetMapping("/list")
    @Operation(summary = "查询当前用户地址")
    public Result<List<UserAddressVO>> list() {
        Long userId = getCurrentUserId();
        List<UserAddressVO> list = userAddressService.listAddresses(userId);
        return Result.success(list);
    }

    @PutMapping("/{id}/default")
    @Operation(summary = "设为默认地址")
    public Result<Void> setDefault(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        userAddressService.setDefault(userId, id);
        return Result.success();
    }

    private Long getCurrentUserId() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUserId();
    }
}
