package com.heritage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("product_image")
public class ProductImage {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("product_id")
    private Long productId;

    @TableField("image_url")
    private String imageUrl;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("is_cover")
    private Integer isCover;

    @TableField("create_time")
    private LocalDateTime createTime;
}

