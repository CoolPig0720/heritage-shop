package com.heritage.controller;

import com.heritage.common.Result;
import com.heritage.dto.CertificateRequest;
import com.heritage.entity.Certificate;
import com.heritage.security.CustomUserDetails;
import com.heritage.service.CertificateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/certificate")
@RequiredArgsConstructor
@Tag(name = "实名认证", description = "用户实名认证相关接口")
public class CertificateController {

    private final CertificateService certificateService;

    @PostMapping
    @Operation(summary = "提交实名认证")
    public Result<Void> verifyCertificate(@Valid @RequestBody CertificateRequest request) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getUserId();
        certificateService.verifyCertificate(userId, request);
        return Result.success();
    }

    @GetMapping
    @Operation(summary = "获取当前用户认证信息")
    public Result<Certificate> getCertification() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getUserId();
        Certificate certificate = certificateService.getCertificateInfoByUserId(userId);
        return Result.success(certificate);
    }

    @PutMapping
    @Operation(summary = "更新认证信息")
    public Result<Void> updateCertification(@Valid @RequestBody CertificateRequest request) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getUserId();
        certificateService.updateCertification(userId, request);
        return Result.success();
    }

    @GetMapping("/{certificateNumber}")
    @Operation(summary = "获取证件信息")
    public Result<Certificate> getCertificateInfo(@PathVariable String certificateNumber) {
        Certificate certificate = certificateService.getCertificateInfo(certificateNumber);
        return Result.success(certificate);
    }
}
