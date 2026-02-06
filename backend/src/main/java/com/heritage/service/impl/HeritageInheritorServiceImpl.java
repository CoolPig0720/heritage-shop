package com.heritage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heritage.common.BusinessException;
import com.heritage.dto.HeritageInheritorAdminVO;
import com.heritage.dto.HeritageInheritorCreateRequest;
import com.heritage.dto.HeritageInheritorDetailVO;
import com.heritage.dto.HeritageInheritorSimpleVO;
import com.heritage.dto.HeritageInheritorUpdateRequest;
import com.heritage.dto.HeritageProjectListItemVO;
import com.heritage.dto.PageQuery;
import com.heritage.entity.HeritageCategory;
import com.heritage.entity.HeritageInheritor;
import com.heritage.entity.HeritageProject;
import com.heritage.entity.HeritageProjectInheritor;
import com.heritage.mapper.HeritageCategoryMapper;
import com.heritage.mapper.HeritageInheritorMapper;
import com.heritage.mapper.HeritageProjectInheritorMapper;
import com.heritage.mapper.HeritageProjectMapper;
import com.heritage.service.HeritageInheritorService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HeritageInheritorServiceImpl extends ServiceImpl<HeritageInheritorMapper, HeritageInheritor> implements HeritageInheritorService {

    private final HeritageProjectInheritorMapper heritageProjectInheritorMapper;
    private final HeritageProjectMapper heritageProjectMapper;
    private final HeritageCategoryMapper heritageCategoryMapper;

    @Override
    public HeritageInheritorDetailVO getInheritorDetail(Long inheritorId) {
        HeritageInheritor inheritor;
        try {
            inheritor = this.getById(inheritorId);
        } catch (DataAccessException e) {
            return null;
        }
        if (inheritor == null) {
            throw new BusinessException("非遗传承人不存在");
        }

        HeritageInheritorDetailVO detail = new HeritageInheritorDetailVO();
        BeanUtils.copyProperties(inheritor, detail);

        List<Long> projectIds;
        try {
            projectIds = heritageProjectInheritorMapper.selectList(new LambdaQueryWrapper<HeritageProjectInheritor>()
                            .eq(HeritageProjectInheritor::getInheritorId, inheritorId))
                    .stream()
                    .map(HeritageProjectInheritor::getProjectId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
        } catch (DataAccessException e) {
            detail.setProjects(Collections.emptyList());
            return detail;
        }

        if (projectIds.isEmpty()) {
            detail.setProjects(Collections.emptyList());
            return detail;
        }

        List<HeritageProject> projects;
        try {
            projects = heritageProjectMapper.selectBatchIds(projectIds);
        } catch (DataAccessException e) {
            detail.setProjects(Collections.emptyList());
            return detail;
        }
        projects.sort((a, b) -> Long.compare(a.getId(), b.getId()));

        List<Long> categoryIds = projects.stream()
                .map(HeritageProject::getCategoryId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        final Map<Long, HeritageCategory> categoryMap;
        if (categoryIds.isEmpty()) {
            categoryMap = Collections.emptyMap();
        } else {
            Map<Long, HeritageCategory> tmp;
            try {
                tmp = heritageCategoryMapper.selectBatchIds(categoryIds).stream()
                        .collect(Collectors.toMap(HeritageCategory::getId, Function.identity(), (a, b) -> a));
            } catch (DataAccessException e) {
                tmp = Collections.emptyMap();
            }
            categoryMap = tmp;
        }

        List<HeritageProjectListItemVO> projectVOs = projects.stream().map(p -> {
            HeritageProjectListItemVO vo = new HeritageProjectListItemVO();
            BeanUtils.copyProperties(p, vo);
            HeritageCategory category = categoryMap.get(p.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
            return vo;
        }).toList();
        detail.setProjects(projectVOs);

        return detail;
    }

    @Override
    public Page<HeritageInheritorSimpleVO> pageInheritors(PageQuery query) {
        Page<HeritageInheritor> page = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<HeritageInheritor> wrapper = new LambdaQueryWrapper<>();

        String kw = query.getKeyword();
        if (kw != null && !kw.trim().isEmpty()) {
            wrapper.like(HeritageInheritor::getName, kw.trim());
        }
        wrapper.orderByDesc(HeritageInheritor::getId);

        Page<HeritageInheritor> inheritorPage;
        try {
            inheritorPage = this.page(page, wrapper);
        } catch (DataAccessException e) {
            Page<HeritageInheritorSimpleVO> empty = new Page<>(query.getPage(), query.getSize(), 0);
            empty.setRecords(Collections.emptyList());
            return empty;
        }

        List<HeritageInheritorSimpleVO> records = inheritorPage.getRecords().stream()
                .filter(Objects::nonNull)
                .map(i -> {
                    HeritageInheritorSimpleVO vo = new HeritageInheritorSimpleVO();
                    BeanUtils.copyProperties(i, vo);
                    return vo;
                }).toList();

        Page<HeritageInheritorSimpleVO> voPage = new Page<>(inheritorPage.getCurrent(), inheritorPage.getSize(), inheritorPage.getTotal());
        voPage.setRecords(records);
        return voPage;
    }

    @Override
    public Page<HeritageInheritorAdminVO> pageAdminInheritors(PageQuery query) {
        Page<HeritageInheritor> page = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<HeritageInheritor> wrapper = new LambdaQueryWrapper<>();

        String kw = query.getKeyword();
        if (kw != null && !kw.trim().isEmpty()) {
            wrapper.like(HeritageInheritor::getName, kw.trim());
        }
        wrapper.orderByDesc(HeritageInheritor::getId);

        Page<HeritageInheritor> inheritorPage;
        try {
            inheritorPage = this.page(page, wrapper);
        } catch (DataAccessException e) {
            Page<HeritageInheritorAdminVO> empty = new Page<>(query.getPage(), query.getSize(), 0);
            empty.setRecords(Collections.emptyList());
            return empty;
        }

        List<HeritageInheritorAdminVO> records = inheritorPage.getRecords().stream()
                .filter(Objects::nonNull)
                .map(i -> {
                    HeritageInheritorAdminVO vo = new HeritageInheritorAdminVO();
                    BeanUtils.copyProperties(i, vo);
                    return vo;
                }).toList();

        Page<HeritageInheritorAdminVO> voPage = new Page<>(inheritorPage.getCurrent(), inheritorPage.getSize(), inheritorPage.getTotal());
        voPage.setRecords(records);
        return voPage;
    }

    @Override
    public HeritageInheritorAdminVO getAdminInheritor(Long inheritorId) {
        HeritageInheritor inheritor;
        try {
            inheritor = this.getById(inheritorId);
        } catch (DataAccessException e) {
            return null;
        }
        if (inheritor == null) {
            throw new BusinessException("非遗传承人不存在");
        }
        HeritageInheritorAdminVO vo = new HeritageInheritorAdminVO();
        BeanUtils.copyProperties(inheritor, vo);
        return vo;
    }

    @Override
    public Long createInheritor(HeritageInheritorCreateRequest request) {
        HeritageInheritor inheritor = new HeritageInheritor();
        inheritor.setName(request.getName());
        inheritor.setGender(request.getGender());
        inheritor.setBirthDate(request.getBirthDate());
        inheritor.setNation(request.getNation());
        inheritor.setPhoto(request.getPhoto());
        inheritor.setDescription(request.getDescription());
        try {
            this.save(inheritor);
        } catch (DataAccessException e) {
            throw new BusinessException("保存失败");
        }
        return inheritor.getId();
    }

    @Override
    public void updateInheritor(Long inheritorId, HeritageInheritorUpdateRequest request) {
        HeritageInheritor existing;
        try {
            existing = this.getById(inheritorId);
        } catch (DataAccessException e) {
            throw new BusinessException("非遗数据未初始化");
        }
        if (existing == null) {
            throw new BusinessException("非遗传承人不存在");
        }

        existing.setName(request.getName());
        existing.setGender(request.getGender());
        existing.setBirthDate(request.getBirthDate());
        existing.setNation(request.getNation());
        existing.setPhoto(request.getPhoto());
        existing.setDescription(request.getDescription());

        try {
            this.updateById(existing);
        } catch (DataAccessException e) {
            throw new BusinessException("保存失败");
        }
    }

    @Override
    @Transactional
    public void deleteInheritor(Long inheritorId) {
        HeritageInheritor existing;
        try {
            existing = this.getById(inheritorId);
        } catch (DataAccessException e) {
            throw new BusinessException("非遗数据未初始化");
        }
        if (existing == null) {
            throw new BusinessException("非遗传承人不存在");
        }

        try {
            heritageProjectInheritorMapper.delete(new LambdaQueryWrapper<HeritageProjectInheritor>()
                    .eq(HeritageProjectInheritor::getInheritorId, inheritorId));
            this.removeById(inheritorId);
        } catch (DataAccessException e) {
            throw new BusinessException("删除失败");
        }
    }
}
