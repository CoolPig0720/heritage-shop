package com.heritage.controller;

import com.heritage.common.Result;
import com.heritage.dto.PaymentActionRequest;
import com.heritage.dto.PaymentCreateRequest;
import com.heritage.dto.PaymentVO;
import com.heritage.security.CustomUserDetails;
import com.heritage.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "支付", description = "支付相关接口")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "创建支付单")
    public Result<PaymentVO> create(@Valid @RequestBody PaymentCreateRequest request) {
        Long userId = getCurrentUserId();
        return Result.success(paymentService.createPayment(userId, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询支付单状态")
    public Result<PaymentVO> get(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        return Result.success(paymentService.getMyPayment(userId, id));
    }

    @PostMapping("/{id}/mock-pay")
    @Operation(summary = "模拟收银台动作确认/取消")
    public Result<PaymentVO> mockPay(@PathVariable Long id, @Valid @RequestBody PaymentActionRequest request) {
        Long userId = getCurrentUserId();
        return Result.success(paymentService.mockPay(userId, id, request));
    }

    private Long getCurrentUserId() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUserId();
    }
}

