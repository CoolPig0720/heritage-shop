package com.heritage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heritage.common.BusinessException;
import com.heritage.dto.HeritageCategoryCreateRequest;
import com.heritage.dto.HeritageCategoryTreeVO;
import com.heritage.entity.HeritageCategory;
import com.heritage.mapper.HeritageCategoryMapper;
import com.heritage.service.HeritageCategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HeritageCategoryServiceImpl extends ServiceImpl<HeritageCategoryMapper, HeritageCategory> implements HeritageCategoryService {

    @Override
    public List<HeritageCategoryTreeVO> getCategoryTree() {
        List<HeritageCategory> categories;
        try {
            categories = this.list(new LambdaQueryWrapper<HeritageCategory>()
                    .orderByAsc(HeritageCategory::getSortOrder)
                    .orderByAsc(HeritageCategory::getId));
        } catch (DataAccessException e) {
            return Collections.emptyList();
        }

        Map<Long, HeritageCategoryTreeVO> nodeMap = new HashMap<>(categories.size());
        for (HeritageCategory category : categories) {
            HeritageCategoryTreeVO node = new HeritageCategoryTreeVO();
            BeanUtils.copyProperties(category, node);
            nodeMap.put(category.getId(), node);
        }

        List<HeritageCategoryTreeVO> roots = new ArrayList<>();
        for (HeritageCategory category : categories) {
            HeritageCategoryTreeVO node = nodeMap.get(category.getId());
            Long parentId = category.getParentId();
            if (parentId == null) {
                roots.add(node);
                continue;
            }

            HeritageCategoryTreeVO parent = nodeMap.get(parentId);
            if (parent == null) {
                roots.add(node);
                continue;
            }
            parent.getChildren().add(node);
        }

        return roots;
    }

    @Override
    public Long createCategory(HeritageCategoryCreateRequest request) {
        Long parentId = request.getParentId();
        if (parentId != null) {
            HeritageCategory parent;
            try {
                parent = this.getById(parentId);
            } catch (DataAccessException e) {
                throw new BusinessException("保存失败");
            }
            if (parent == null) {
                throw new BusinessException("父级类型不存在");
            }
        }

        HeritageCategory category = new HeritageCategory();
        category.setName(request.getName().trim());
        category.setParentId(parentId);
        category.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());

        try {
            this.save(category);
        } catch (DataAccessException e) {
            throw new BusinessException("保存失败");
        }
        return category.getId();
    }
}
