package com.heritage.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.heritage.dto.PageQuery;
import com.heritage.dto.ProductCreateRequest;
import com.heritage.dto.ProductImageAddRequest;
import com.heritage.dto.ProductImageUpdateRequest;
import com.heritage.dto.ProductImageVO;
import com.heritage.dto.ProductUpdateRequest;
import com.heritage.dto.ProductVO;
import com.heritage.entity.Product;

import java.util.List;

public interface ProductService extends IService<Product> {

    Long createProduct(Long operatorUserId, boolean isAdmin, ProductCreateRequest request);

    void updateProduct(Long operatorUserId, boolean isAdmin, Long productId, ProductUpdateRequest request);

    void updateProductStatus(Long operatorUserId, boolean isAdmin, Long productId, Integer status);

    void deleteProduct(Long operatorUserId, boolean isAdmin, Long productId);

    Page<ProductVO> pageManageProducts(Long operatorUserId, boolean isAdmin, PageQuery query, Integer status);

    ProductVO getProductDetail(Long productId);

    List<ProductVO> getRandomOnSaleProducts(Integer count);

    List<ProductImageVO> listManageProductImages(Long operatorUserId, boolean isAdmin, Long productId);

    List<ProductImageVO> addManageProductImages(Long operatorUserId, boolean isAdmin, Long productId, ProductImageAddRequest request);

    void updateManageProductImage(Long operatorUserId, boolean isAdmin, Long imageId, ProductImageUpdateRequest request);

    void deleteManageProductImage(Long operatorUserId, boolean isAdmin, Long imageId);
}
