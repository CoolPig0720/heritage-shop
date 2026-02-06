package com.heritage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heritage.dto.HeritageCategoryCreateRequest;
import com.heritage.dto.HeritageCategoryTreeVO;
import com.heritage.entity.HeritageCategory;

import java.util.List;

public interface HeritageCategoryService extends IService<HeritageCategory> {

    List<HeritageCategoryTreeVO> getCategoryTree();

    Long createCategory(HeritageCategoryCreateRequest request);
}
