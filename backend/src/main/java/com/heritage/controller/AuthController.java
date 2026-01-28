package com.heritage.controller;

import com.heritage.common.Result;
import com.heritage.dto.LoginRequest;
import com.heritage.dto.LoginResponse;
import com.heritage.dto.RegisterRequest;
import com.heritage.service.UserService;
import com.heritage.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户注册、登录等认证相关接口")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return Result.success();
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        String token = jwtUtil.generateToken(
                org.springframework.security.core.userdetails.User.builder()
                        .username(response.getUserInfo().getAccount())
                        .password("")
                        .authorities(Collections.emptyList())
                        .build()
        );
        response.setToken(token);
        return Result.success(response);
    }
}
