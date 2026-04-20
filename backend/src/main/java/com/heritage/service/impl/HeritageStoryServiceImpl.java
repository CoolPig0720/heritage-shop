package com.heritage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heritage.common.BusinessException;
import com.heritage.dto.HeritageStoryCreateRequest;
import com.heritage.dto.HeritageStoryUpdateRequest;
import com.heritage.dto.HeritageStoryVO;
import com.heritage.dto.PageQuery;
import com.heritage.entity.HeritageProject;
import com.heritage.entity.HeritageStory;
import com.heritage.mapper.HeritageProjectMapper;
import com.heritage.mapper.HeritageStoryMapper;
import com.heritage.service.HeritageStoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HeritageStoryServiceImpl extends ServiceImpl<HeritageStoryMapper, HeritageStory>
        implements HeritageStoryService {

    private final HeritageProjectMapper heritageProjectMapper;

    @Override
    public Long createHeritageStory(HeritageStoryCreateRequest request) {
        // 校验关联非遗项目（如果提供了的话）
        if (request.getHeritageProjectId() != null) {
            HeritageProject project;
            try {
                project = heritageProjectMapper.selectById(request.getHeritageProjectId());
            } catch (DataAccessException e) {
                project = null;
            }
            if (project == null) {
                // 关联项目不存在则忽略关联
                request.setHeritageProjectId(null);
            }
        }

        HeritageStory entity = new HeritageStory();
        entity.setTitle(request.getTitle().trim());
        entity.setContent(request.getContent());
        entity.setHeritageProjectId(request.getHeritageProjectId());
        entity.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        entity.setIsTop(request.getIsTop() != null ? request.getIsTop() : 0);

        try {
            this.save(entity);
        } catch (DataAccessException e) {
            throw new BusinessException("保存失败");
        }
        return entity.getId();
    }

    @Override
    public void updateHeritageStory(Long id, HeritageStoryUpdateRequest request) {
        HeritageStory existing;
        try {
            existing = this.getById(id);
        } catch (DataAccessException e) {
            throw new BusinessException("数据未初始化");
        }
        if (existing == null) {
            throw new BusinessException("非遗故事不存在");
        }

        if (request.getTitle() != null) {
            existing.setTitle(request.getTitle().trim());
        }
        if (request.getContent() != null) {
            existing.setContent(request.getContent());
        }
        if (request.getHeritageProjectId() != null) {
            // 校验关联非遗项目
            HeritageProject project;
            try {
                project = heritageProjectMapper.selectById(request.getHeritageProjectId());
            } catch (DataAccessException e) {
                project = null;
            }
            if (project != null) {
                existing.setHeritageProjectId(request.getHeritageProjectId());
            }
            // 项目不存在则忽略更新关联字段
        }
        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }
        if (request.getIsTop() != null) {
            existing.setIsTop(request.getIsTop());
        }

        try {
            this.updateById(existing);
        } catch (DataAccessException e) {
            throw new BusinessException("保存失败");
        }
    }

    @Override
    public void deleteHeritageStory(Long id) {
        HeritageStory existing;
        try {
            existing = this.getById(id);
        } catch (DataAccessException e) {
            throw new BusinessException("数据未初始化");
        }
        if (existing == null) {
            throw new BusinessException("非遗故事不存在");
        }

        try {
            this.removeById(id);
        } catch (DataAccessException e) {
            throw new BusinessException("删除失败");
        }
    }

    @Override
    public Page<HeritageStoryVO> getManagePage(PageQuery query, Integer status, Long heritageProjectId) {
        Page<HeritageStory> page = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<HeritageStory> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(HeritageStory::getStatus, status);
        }
        if (heritageProjectId != null) {
            wrapper.eq(HeritageStory::getHeritageProjectId, heritageProjectId);
        }
        if (query.getKeyword() != null && !query.getKeyword().trim().isEmpty()) {
            wrapper.like(HeritageStory::getTitle, query.getKeyword().trim());
        }

        wrapper.orderByDesc(HeritageStory::getIsTop)
                .orderByDesc(HeritageStory::getCreateTime);

        Page<HeritageStory> entityPage;
        try {
            entityPage = this.page(page, wrapper);
        } catch (DataAccessException e) {
            Page<HeritageStoryVO> empty = new Page<>(query.getPage(), query.getSize(), 0);
            empty.setRecords(Collections.emptyList());
            return empty;
        }

        // 批量加载关联项目名称
        java.util.Map<Long, String> projectNameMap = loadProjectNameMap(entityPage.getRecords());

        Page<HeritageStoryVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(entityPage.getRecords().stream()
                .map(e -> toVO(e, projectNameMap))
                .toList());
        return voPage;
    }

    @Override
    public Page<HeritageStoryVO> getPublishedPage(PageQuery query) {
        Page<HeritageStory> page = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<HeritageStory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HeritageStory::getStatus, 1)
                .orderByDesc(HeritageStory::getIsTop)
                .orderByDesc(HeritageStory::getCreateTime);

        Page<HeritageStory> entityPage;
        try {
            entityPage = this.page(page, wrapper);
        } catch (DataAccessException e) {
            Page<HeritageStoryVO> empty = new Page<>(query.getPage(), query.getSize(), 0);
            empty.setRecords(Collections.emptyList());
            return empty;
        }

        java.util.Map<Long, String> projectNameMap = loadProjectNameMap(entityPage.getRecords());

        Page<HeritageStoryVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(entityPage.getRecords().stream()
                .map(e -> toVO(e, projectNameMap))
                .toList());
        return voPage;
    }

    @Override
    public HeritageStoryVO getDetail(Long id) {
        HeritageStory entity;
        try {
            entity = this.getById(id);
        } catch (DataAccessException e) {
            return null;
        }
        if (entity == null) {
            throw new BusinessException("非遗故事不存在");
        }

        HeritageStoryVO vo = new HeritageStoryVO();
        BeanUtils.copyProperties(entity, vo);

        if (entity.getHeritageProjectId() != null) {
            try {
                HeritageProject project = heritageProjectMapper.selectById(entity.getHeritageProjectId());
                if (project != null) {
                    vo.setHeritageProjectName(project.getName());
                }
            } catch (DataAccessException e) {
                // ignore
            }
        }

        return vo;
    }

    private java.util.Map<Long, String> loadProjectNameMap(java.util.List<HeritageStory> records) {
        java.util.List<Long> projectIds = records.stream()
                .map(HeritageStory::getHeritageProjectId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (projectIds.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            return heritageProjectMapper.selectBatchIds(projectIds).stream()
                    .filter(Objects::nonNull)
                    .filter(p -> p.getId() != null)
                    .collect(Collectors.toMap(HeritageProject::getId, HeritageProject::getName, (a, b) -> a));
        } catch (DataAccessException e) {
            return Collections.emptyMap();
        }
    }

    private HeritageStoryVO toVO(HeritageStory entity, java.util.Map<Long, String> projectNameMap) {
        HeritageStoryVO vo = new HeritageStoryVO();
        BeanUtils.copyProperties(entity, vo);
        if (entity.getHeritageProjectId() != null) {
            vo.setHeritageProjectName(projectNameMap.getOrDefault(entity.getHeritageProjectId(), null));
        }
        return vo;
    }
}
