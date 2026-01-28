package com.heritage.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heritage.common.Result;
import com.heritage.dto.PageQuery;
import com.heritage.dto.ProductCreateRequest;
import com.heritage.dto.ProductImageAddRequest;
import com.heritage.dto.ProductImageUpdateRequest;
import com.heritage.dto.ProductImageVO;
import com.heritage.dto.ProductStatusUpdateRequest;
import com.heritage.dto.ProductUpdateRequest;
import com.heritage.dto.ProductVO;
import com.heritage.security.CustomUserDetails;
import com.heritage.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "商品管理", description = "商品管理相关接口")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @Operation(summary = "新增商品（管理员/商家）")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MERCHANT')")
    public Result<Long> create(@Valid @RequestBody ProductCreateRequest request) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getUserId();
        boolean isAdmin = userDetails.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        Long productId = productService.createProduct(userId, isAdmin, request);
        return Result.success(productId);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新商品（管理员/商家）")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MERCHANT')")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ProductUpdateRequest request) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getUserId();
        boolean isAdmin = userDetails.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        productService.updateProduct(userId, isAdmin, id, request);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "商品上下架（管理员/商家）")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MERCHANT')")
    public Result<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody ProductStatusUpdateRequest request) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getUserId();
        boolean isAdmin = userDetails.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        productService.updateProductStatus(userId, isAdmin, id, request.getStatus());
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除商品（管理员/商家）")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MERCHANT')")
    public Result<Void> delete(@PathVariable Long id) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getUserId();
        boolean isAdmin = userDetails.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        productService.deleteProduct(userId, isAdmin, id);
        return Result.success();
    }

    @GetMapping
    @Operation(summary = "管理端商品分页列表（管理员/商家）")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MERCHANT')")
    public Result<Page<ProductVO>> page(PageQuery query, @RequestParam(required = false) Integer status) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getUserId();
        boolean isAdmin = userDetails.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        Page<ProductVO> page = productService.pageManageProducts(userId, isAdmin, query, status);
        return Result.success(page);
    }

    @GetMapping("/{id}/images")
    @Operation(summary = "获取商品图片列表（管理员/商家）")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MERCHANT')")
    public Result<List<ProductImageVO>> listImages(@PathVariable Long id) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getUserId();
        boolean isAdmin = userDetails.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        return Result.success(productService.listManageProductImages(userId, isAdmin, id));
    }

    @PostMapping("/{id}/images")
    @Operation(summary = "新增商品图片（管理员/商家）")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MERCHANT')")
    public Result<List<ProductImageVO>> addImages(@PathVariable Long id, @Valid @RequestBody ProductImageAddRequest request) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getUserId();
        boolean isAdmin = userDetails.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        return Result.success(productService.addManageProductImages(userId, isAdmin, id, request));
    }

    @PutMapping("/images/{imageId}")
    @Operation(summary = "更新商品图片（管理员/商家）")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MERCHANT')")
    public Result<Void> updateImage(@PathVariable Long imageId, @Valid @RequestBody ProductImageUpdateRequest request) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getUserId();
        boolean isAdmin = userDetails.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        productService.updateManageProductImage(userId, isAdmin, imageId, request);
        return Result.success();
    }

    @DeleteMapping("/images/{imageId}")
    @Operation(summary = "删除商品图片（管理员/商家）")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MERCHANT')")
    public Result<Void> deleteImage(@PathVariable Long imageId) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getUserId();
        boolean isAdmin = userDetails.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        productService.deleteManageProductImage(userId, isAdmin, imageId);
        return Result.success();
    }
}
