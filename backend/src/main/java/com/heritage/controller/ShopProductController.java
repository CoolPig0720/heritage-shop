package com.heritage.controller;

import com.heritage.common.Result;
import com.heritage.dto.ProductVO;
import com.heritage.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/shop")
@RequiredArgsConstructor
@Tag(name = "商城展示", description = "前台商品展示相关接口")
public class ShopProductController {

    private final ProductService productService;

    @GetMapping("/recommend")
    @Operation(summary = "随机推荐商品")
    public Result<List<ProductVO>> recommend(@RequestParam(defaultValue = "20") Integer count) {
        return Result.success(productService.getRandomOnSaleProducts(count));
    }

    @GetMapping("/products/{id}")
    @Operation(summary = "商品详情")
    public Result<ProductVO> detail(@PathVariable Long id) {
        return Result.success(productService.getProductDetail(id));
    }
}

