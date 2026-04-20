package com.heritage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heritage.dto.ProductRatingRequest;
import com.heritage.dto.ProductRatingVO;
import com.heritage.entity.ProductRating;

public interface ProductRatingService extends IService<ProductRating> {

    ProductRatingVO getProductRating(Long productId, Long currentUserId);

    void submitRating(Long userId, ProductRatingRequest request);

    Integer getMyRating(Long userId, Long productId);
}
