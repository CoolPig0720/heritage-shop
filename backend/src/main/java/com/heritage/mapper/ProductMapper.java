package com.heritage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.heritage.entity.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}

