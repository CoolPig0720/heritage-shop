package com.heritage.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class ProductRatingVO {

    private Long productId;

    private BigDecimal avgRating;

    private Integer ratingCount;

    /**
     * 评分分布，key为星级(1-5)，value为人数
     */
    private Map<Integer, Integer> distribution;

    /**
     * 当前登录用户的评分，未登录或未评分为null
     */
    private Integer myRating;
}
