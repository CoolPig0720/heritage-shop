package com.heritage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heritage.common.BusinessException;
import com.heritage.dto.ProductRatingRequest;
import com.heritage.dto.ProductRatingVO;
import com.heritage.entity.Product;
import com.heritage.entity.ProductRating;
import com.heritage.mapper.ProductMapper;
import com.heritage.mapper.ProductRatingMapper;
import com.heritage.service.ProductRatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductRatingServiceImpl extends ServiceImpl<ProductRatingMapper, ProductRating>
        implements ProductRatingService {

    private final ProductRatingMapper ratingMapper;
    private final ProductMapper productMapper;

    @Override
    public ProductRatingVO getProductRating(Long productId, Long currentUserId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        ProductRatingVO vo = new ProductRatingVO();
        vo.setProductId(productId);
        vo.setAvgRating(product.getAvgRating() != null ? product.getAvgRating() : BigDecimal.ZERO);
        vo.setRatingCount(product.getRatingCount() != null ? product.getRatingCount() : 0);

        // 查询评分分布
        Map<Integer, Integer> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            distribution.put(i, 0);
        }
        LambdaQueryWrapper<ProductRating> distWrapper = new LambdaQueryWrapper<>();
        distWrapper.eq(ProductRating::getProductId, productId)
                .select(ProductRating::getRating);
        List<ProductRating> allRatings = ratingMapper.selectList(distWrapper);
        for (ProductRating r : allRatings) {
            distribution.merge(r.getRating(), 1, Integer::sum);
        }
        vo.setDistribution(distribution);

        // 当前用户评分
        if (currentUserId != null) {
            vo.setMyRating(getMyRating(currentUserId, productId));
        }

        return vo;
    }

    @Override
    @Transactional
    public void submitRating(Long userId, ProductRatingRequest request) {
        Product product = productMapper.selectById(request.getProductId());
        if (product == null || product.getStatus() != 1) {
            throw new BusinessException("商品不存在或已下架");
        }

        // 查询是否已有评分
        LambdaQueryWrapper<ProductRating> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductRating::getProductId, request.getProductId())
                .eq(ProductRating::getUserId, userId);
        ProductRating existing = ratingMapper.selectOne(wrapper);

        if (existing != null) {
            // 更新评分
            existing.setRating(request.getRating());
            ratingMapper.updateById(existing);
        } else {
            // 新增评分
            ProductRating rating = new ProductRating();
            rating.setProductId(request.getProductId());
            rating.setUserId(userId);
            rating.setRating(request.getRating());
            ratingMapper.insert(rating);
        }

        // 重新计算商品的 avg_rating 和 rating_count
        recalculateProductRating(request.getProductId());
    }

    @Override
    public Integer getMyRating(Long userId, Long productId) {
        LambdaQueryWrapper<ProductRating> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductRating::getProductId, productId)
                .eq(ProductRating::getUserId, userId);
        ProductRating rating = ratingMapper.selectOne(wrapper);
        return rating != null ? rating.getRating() : null;
    }

    /**
     * 重新计算商品的 avg_rating 和 rating_count 并更新
     */
    private void recalculateProductRating(Long productId) {
        LambdaQueryWrapper<ProductRating> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductRating::getProductId, productId);
        Long count = ratingMapper.selectCount(wrapper);

        BigDecimal avg = BigDecimal.ZERO;
        if (count > 0) {
            // 查询所有评分求平均
            LambdaQueryWrapper<ProductRating> sumWrapper = new LambdaQueryWrapper<>();
            sumWrapper.eq(ProductRating::getProductId, productId)
                    .select(ProductRating::getRating);
            List<ProductRating> ratings = ratingMapper.selectList(sumWrapper);
            int sum = ratings.stream().mapToInt(ProductRating::getRating).sum();
            avg = BigDecimal.valueOf(sum)
                    .divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
        }

        Product product = productMapper.selectById(productId);
        product.setAvgRating(avg);
        product.setRatingCount(count.intValue());
        productMapper.updateById(product);
    }
}
