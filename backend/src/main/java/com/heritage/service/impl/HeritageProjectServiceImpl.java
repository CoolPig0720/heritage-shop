package com.heritage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heritage.common.BusinessException;
import com.heritage.dto.HeritageInheritorSimpleVO;
import com.heritage.dto.HeritageProjectCreateRequest;
import com.heritage.dto.HeritageProjectDetailVO;
import com.heritage.dto.HeritageProjectListItemVO;
import com.heritage.dto.HeritageProjectMediaCreateRequest;
import com.heritage.dto.HeritageProjectMediaUpdateRequest;
import com.heritage.dto.HeritageProjectMediaVO;
import com.heritage.dto.HeritageProjectUpdateRequest;
import com.heritage.dto.PageQuery;
import com.heritage.entity.HeritageCategory;
import com.heritage.entity.HeritageInheritor;
import com.heritage.entity.HeritageProject;
import com.heritage.entity.HeritageProjectInheritor;
import com.heritage.entity.HeritageProjectMedia;
import com.heritage.mapper.HeritageCategoryMapper;
import com.heritage.mapper.HeritageInheritorMapper;
import com.heritage.mapper.HeritageProjectInheritorMapper;
import com.heritage.mapper.HeritageProjectMapper;
import com.heritage.mapper.HeritageProjectMediaMapper;
import com.heritage.service.HeritageProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HeritageProjectServiceImpl extends ServiceImpl<HeritageProjectMapper, HeritageProject> implements HeritageProjectService {

    private final HeritageCategoryMapper heritageCategoryMapper;
    private final HeritageProjectMediaMapper heritageProjectMediaMapper;
    private final HeritageProjectInheritorMapper heritageProjectInheritorMapper;
    private final HeritageInheritorMapper heritageInheritorMapper;

    private static final List<String> allowedMediaTypes = List.of("DISPLAY_IMAGE", "PROCESS_IMAGE", "VIDEO");

    @Override
    public Page<HeritageProjectListItemVO> pageProjects(PageQuery query, Long categoryId) {
        Page<HeritageProject> page = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<HeritageProject> wrapper = new LambdaQueryWrapper<>();

        if (categoryId != null) {
            wrapper.eq(HeritageProject::getCategoryId, categoryId);
        }
        if (query.getKeyword() != null && !query.getKeyword().trim().isEmpty()) {
            wrapper.like(HeritageProject::getName, query.getKeyword().trim());
        }

        wrapper.orderByDesc(HeritageProject::getId);
        Page<HeritageProject> projectPage;
        try {
            projectPage = this.page(page, wrapper);
        } catch (DataAccessException e) {
            Page<HeritageProjectListItemVO> empty = new Page<>(query.getPage(), query.getSize(), 0);
            empty.setRecords(Collections.emptyList());
            return empty;
        }
        List<HeritageProject> records = projectPage.getRecords();

        Map<Long, HeritageCategory> categoryMap = loadCategoryMap(records);
        ProjectInheritorAgg inheritorAgg = loadProjectInheritors(records);
        List<HeritageProjectListItemVO> voRecords = records.stream()
                .map(p -> toListItemVO(p, categoryMap, inheritorAgg))
                .toList();

        Page<HeritageProjectListItemVO> voPage = new Page<>(projectPage.getCurrent(), projectPage.getSize(), projectPage.getTotal());
        voPage.setRecords(voRecords);
        return voPage;
    }

    @Override
    public HeritageProjectDetailVO getProjectDetail(Long projectId) {
        HeritageProject project;
        try {
            project = this.getById(projectId);
        } catch (DataAccessException e) {
            return null;
        }
        if (project == null) {
            throw new BusinessException("非遗项目不存在");
        }

        HeritageCategory category;
        try {
            category = heritageCategoryMapper.selectById(project.getCategoryId());
        } catch (DataAccessException e) {
            category = null;
        }

        HeritageProjectDetailVO detail = new HeritageProjectDetailVO();
        BeanUtils.copyProperties(project, detail);
        if (category != null) {
            detail.setCategoryName(category.getName());
        }

        List<HeritageProjectMedia> medias;
        try {
            medias = heritageProjectMediaMapper.selectList(new LambdaQueryWrapper<HeritageProjectMedia>()
                    .eq(HeritageProjectMedia::getProjectId, projectId)
                    .orderByAsc(HeritageProjectMedia::getSortOrder)
                    .orderByAsc(HeritageProjectMedia::getId));
        } catch (DataAccessException e) {
            medias = Collections.emptyList();
        }
        List<HeritageProjectMediaVO> mediaVOs = medias.stream().map(m -> {
            HeritageProjectMediaVO vo = new HeritageProjectMediaVO();
            BeanUtils.copyProperties(m, vo);
            return vo;
        }).toList();
        detail.setMedias(mediaVOs);

        List<Long> inheritorIds;
        try {
            inheritorIds = heritageProjectInheritorMapper.selectList(new LambdaQueryWrapper<HeritageProjectInheritor>()
                            .eq(HeritageProjectInheritor::getProjectId, projectId))
                    .stream()
                    .map(HeritageProjectInheritor::getInheritorId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
        } catch (DataAccessException e) {
            inheritorIds = Collections.emptyList();
        }

        if (!inheritorIds.isEmpty()) {
            try {
                List<HeritageInheritor> inheritors = heritageInheritorMapper.selectBatchIds(inheritorIds);
                inheritors.sort((a, b) -> Long.compare(a.getId(), b.getId()));
                List<HeritageInheritorSimpleVO> inheritorVOs = inheritors.stream().map(i -> {
                    HeritageInheritorSimpleVO vo = new HeritageInheritorSimpleVO();
                    BeanUtils.copyProperties(i, vo);
                    return vo;
                }).toList();
                detail.setInheritors(inheritorVOs);
            } catch (DataAccessException e) {
                detail.setInheritors(Collections.emptyList());
            }
        } else {
            detail.setInheritors(Collections.emptyList());
        }

        return detail;
    }

    @Override
    public Long createProject(HeritageProjectCreateRequest request) {
        HeritageCategory category;
        try {
            category = heritageCategoryMapper.selectById(request.getCategoryId());
        } catch (DataAccessException e) {
            throw new BusinessException("非遗数据未初始化");
        }
        if (category == null) {
            throw new BusinessException("类型不存在");
        }

        HeritageProject project = new HeritageProject();
        project.setCategoryId(request.getCategoryId());
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setApplyUnit(request.getApplyUnit());
        project.setProtectUnit(request.getProtectUnit());

        try {
            this.save(project);
        } catch (DataAccessException e) {
            throw new BusinessException("保存失败");
        }
        return project.getId();
    }

    @Override
    public void updateProject(Long projectId, HeritageProjectUpdateRequest request) {
        HeritageProject existing;
        try {
            existing = this.getById(projectId);
        } catch (DataAccessException e) {
            throw new BusinessException("非遗数据未初始化");
        }
        if (existing == null) {
            throw new BusinessException("非遗项目不存在");
        }

        HeritageCategory category;
        try {
            category = heritageCategoryMapper.selectById(request.getCategoryId());
        } catch (DataAccessException e) {
            throw new BusinessException("非遗数据未初始化");
        }
        if (category == null) {
            throw new BusinessException("类型不存在");
        }

        existing.setCategoryId(request.getCategoryId());
        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        existing.setApplyUnit(request.getApplyUnit());
        existing.setProtectUnit(request.getProtectUnit());

        try {
            this.updateById(existing);
        } catch (DataAccessException e) {
            throw new BusinessException("保存失败");
        }
    }

    @Override
    @Transactional
    public void updateProjectInheritors(Long projectId, List<Long> inheritorIds) {
        HeritageProject existing;
        try {
            existing = this.getById(projectId);
        } catch (DataAccessException e) {
            throw new BusinessException("非遗数据未初始化");
        }
        if (existing == null) {
            throw new BusinessException("非遗项目不存在");
        }

        LinkedHashSet<Long> uniqueIds = new LinkedHashSet<>();
        if (inheritorIds != null) {
            for (Long id : inheritorIds) {
                if (id != null) {
                    uniqueIds.add(id);
                }
            }
        }

        if (!uniqueIds.isEmpty()) {
            List<HeritageInheritor> inheritors;
            try {
                inheritors = heritageInheritorMapper.selectBatchIds(new ArrayList<>(uniqueIds));
            } catch (DataAccessException e) {
                throw new BusinessException("非遗数据未初始化");
            }

            LinkedHashSet<Long> existIds = inheritors.stream()
                    .filter(Objects::nonNull)
                    .map(HeritageInheritor::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            if (existIds.size() != uniqueIds.size()) {
                throw new BusinessException("传承人不存在");
            }
        }

        try {
            heritageProjectInheritorMapper.delete(new LambdaQueryWrapper<HeritageProjectInheritor>()
                    .eq(HeritageProjectInheritor::getProjectId, projectId));
            for (Long inheritorId : uniqueIds) {
                HeritageProjectInheritor relation = new HeritageProjectInheritor();
                relation.setProjectId(projectId);
                relation.setInheritorId(inheritorId);
                heritageProjectInheritorMapper.insert(relation);
            }
        } catch (DataAccessException e) {
            throw new BusinessException("保存失败");
        }
    }

    @Override
    public Long addProjectMedia(Long projectId, HeritageProjectMediaCreateRequest request) {
        HeritageProject existing;
        try {
            existing = this.getById(projectId);
        } catch (DataAccessException e) {
            throw new BusinessException("非遗数据未初始化");
        }
        if (existing == null) {
            throw new BusinessException("非遗项目不存在");
        }

        String mediaType = request.getMediaType() == null ? "" : request.getMediaType().trim();
        if (!allowedMediaTypes.contains(mediaType)) {
            throw new BusinessException("媒体类型不支持");
        }
        String mediaUrl = request.getMediaUrl() == null ? "" : request.getMediaUrl().trim();
        if (mediaUrl.isEmpty()) {
            throw new BusinessException("媒体地址不能为空");
        }

        HeritageProjectMedia media = new HeritageProjectMedia();
        media.setProjectId(projectId);
        media.setMediaType(mediaType);
        media.setMediaUrl(mediaUrl);
        media.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());

        try {
            heritageProjectMediaMapper.insert(media);
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null) {
                msg = msg.trim();
                if (msg.length() > 180) {
                    msg = msg.substring(0, 180);
                }
            }
            throw new BusinessException(msg == null || msg.isEmpty() ? "保存失败" : "保存失败：" + msg);
        }
        return media.getId();
    }

    @Override
    public void updateProjectMedia(Long mediaId, HeritageProjectMediaUpdateRequest request) {
        HeritageProjectMedia existing;
        try {
            existing = heritageProjectMediaMapper.selectById(mediaId);
        } catch (DataAccessException e) {
            throw new BusinessException("非遗数据未初始化");
        }
        if (existing == null) {
            throw new BusinessException("媒体不存在");
        }

        if (request.getMediaType() != null && !request.getMediaType().trim().isEmpty()) {
            String mediaType = request.getMediaType().trim();
            if (!allowedMediaTypes.contains(mediaType)) {
                throw new BusinessException("媒体类型不支持");
            }
            existing.setMediaType(mediaType);
        }
        if (request.getMediaUrl() != null && !request.getMediaUrl().trim().isEmpty()) {
            existing.setMediaUrl(request.getMediaUrl().trim());
        }
        if (request.getSortOrder() != null) {
            existing.setSortOrder(request.getSortOrder());
        }

        try {
            heritageProjectMediaMapper.updateById(existing);
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null) {
                msg = msg.trim();
                if (msg.length() > 180) {
                    msg = msg.substring(0, 180);
                }
            }
            throw new BusinessException(msg == null || msg.isEmpty() ? "保存失败" : "保存失败：" + msg);
        }
    }

    @Override
    public void deleteProjectMedia(Long mediaId) {
        HeritageProjectMedia existing;
        try {
            existing = heritageProjectMediaMapper.selectById(mediaId);
        } catch (DataAccessException e) {
            throw new BusinessException("非遗数据未初始化");
        }
        if (existing == null) {
            throw new BusinessException("媒体不存在");
        }

        try {
            heritageProjectMediaMapper.deleteById(mediaId);
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null) {
                msg = msg.trim();
                if (msg.length() > 180) {
                    msg = msg.substring(0, 180);
                }
            }
            throw new BusinessException(msg == null || msg.isEmpty() ? "删除失败" : "删除失败：" + msg);
        }
    }

    @Override
    @Transactional
    public void deleteProject(Long projectId) {
        HeritageProject existing;
        try {
            existing = this.getById(projectId);
        } catch (DataAccessException e) {
            throw new BusinessException("非遗数据未初始化");
        }
        if (existing == null) {
            throw new BusinessException("非遗项目不存在");
        }

        try {
            heritageProjectMediaMapper.delete(new LambdaQueryWrapper<HeritageProjectMedia>()
                    .eq(HeritageProjectMedia::getProjectId, projectId));
            heritageProjectInheritorMapper.delete(new LambdaQueryWrapper<HeritageProjectInheritor>()
                    .eq(HeritageProjectInheritor::getProjectId, projectId));
            this.removeById(projectId);
        } catch (DataAccessException e) {
            throw new BusinessException("删除失败");
        }
    }

    private Map<Long, HeritageCategory> loadCategoryMap(List<HeritageProject> projects) {
        List<Long> categoryIds = projects.stream()
                .map(HeritageProject::getCategoryId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (categoryIds.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            List<HeritageCategory> categories = heritageCategoryMapper.selectBatchIds(categoryIds);
            return categories.stream().collect(Collectors.toMap(HeritageCategory::getId, Function.identity(), (a, b) -> a));
        } catch (DataAccessException e) {
            return Collections.emptyMap();
        }
    }

    private ProjectInheritorAgg loadProjectInheritors(List<HeritageProject> projects) {
        List<Long> projectIds = projects.stream()
                .map(HeritageProject::getId)
                .filter(Objects::nonNull)
                .toList();
        if (projectIds.isEmpty()) {
            return ProjectInheritorAgg.empty();
        }

        List<HeritageProjectInheritor> relations;
        try {
            relations = heritageProjectInheritorMapper.selectList(new LambdaQueryWrapper<HeritageProjectInheritor>()
                    .in(HeritageProjectInheritor::getProjectId, projectIds)
                    .orderByAsc(HeritageProjectInheritor::getId));
        } catch (DataAccessException e) {
            return ProjectInheritorAgg.empty();
        }

        if (relations.isEmpty()) {
            return ProjectInheritorAgg.empty();
        }

        Map<Long, LinkedHashSet<Long>> projectToInheritorIds = new LinkedHashMap<>();
        LinkedHashSet<Long> inheritorIds = new LinkedHashSet<>();
        for (HeritageProjectInheritor r : relations) {
            Long projectId = r.getProjectId();
            Long inheritorId = r.getInheritorId();
            if (projectId == null || inheritorId == null) {
                continue;
            }
            projectToInheritorIds.computeIfAbsent(projectId, k -> new LinkedHashSet<>()).add(inheritorId);
            inheritorIds.add(inheritorId);
        }
        if (inheritorIds.isEmpty() || projectToInheritorIds.isEmpty()) {
            return ProjectInheritorAgg.empty();
        }

        Map<Long, HeritageInheritor> inheritorMap;
        try {
            inheritorMap = heritageInheritorMapper.selectBatchIds(new ArrayList<>(inheritorIds)).stream()
                    .filter(Objects::nonNull)
                    .filter(i -> i.getId() != null)
                    .collect(Collectors.toMap(HeritageInheritor::getId, Function.identity(), (a, b) -> a));
        } catch (DataAccessException e) {
            inheritorMap = Collections.emptyMap();
        }
        if (inheritorMap.isEmpty()) {
            return ProjectInheritorAgg.empty();
        }

        Map<Long, List<HeritageInheritorSimpleVO>> projectInheritors = new LinkedHashMap<>();
        Map<Long, String> projectInheritorNames = new LinkedHashMap<>();
        for (Map.Entry<Long, LinkedHashSet<Long>> e : projectToInheritorIds.entrySet()) {
            Long projectId = e.getKey();
            LinkedHashSet<Long> ids = e.getValue();
            if (projectId == null || ids == null || ids.isEmpty()) {
                continue;
            }

            List<HeritageInheritorSimpleVO> vos = new ArrayList<>();
            List<String> names = new ArrayList<>();
            for (Long inheritorId : ids) {
                HeritageInheritor inheritor = inheritorMap.get(inheritorId);
                if (inheritor == null) {
                    continue;
                }
                HeritageInheritorSimpleVO vo = new HeritageInheritorSimpleVO();
                BeanUtils.copyProperties(inheritor, vo);
                if (vo.getId() == null) {
                    continue;
                }
                vos.add(vo);
                if (vo.getName() != null && !vo.getName().trim().isEmpty()) {
                    names.add(vo.getName().trim());
                }
            }
            projectInheritors.put(projectId, vos);
            projectInheritorNames.put(projectId, String.join("、", names));
        }

        return new ProjectInheritorAgg(projectInheritorNames, projectInheritors);
    }

    private HeritageProjectListItemVO toListItemVO(HeritageProject project, Map<Long, HeritageCategory> categoryMap, ProjectInheritorAgg inheritorAgg) {
        HeritageProjectListItemVO vo = new HeritageProjectListItemVO();
        BeanUtils.copyProperties(project, vo);
        HeritageCategory category = categoryMap.get(project.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getName());
        }
        vo.setInheritorNames(inheritorAgg.projectInheritorNames.getOrDefault(project.getId(), ""));
        vo.setInheritors(inheritorAgg.projectInheritors.getOrDefault(project.getId(), Collections.emptyList()));
        return vo;
    }

    private static class ProjectInheritorAgg {
        private final Map<Long, String> projectInheritorNames;
        private final Map<Long, List<HeritageInheritorSimpleVO>> projectInheritors;

        private ProjectInheritorAgg(Map<Long, String> projectInheritorNames, Map<Long, List<HeritageInheritorSimpleVO>> projectInheritors) {
            this.projectInheritorNames = projectInheritorNames;
            this.projectInheritors = projectInheritors;
        }

        private static ProjectInheritorAgg empty() {
            return new ProjectInheritorAgg(Collections.emptyMap(), Collections.emptyMap());
        }
    }
}
