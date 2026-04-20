package com.heritage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heritage.common.BusinessException;
import com.heritage.dto.AnnouncementCreateRequest;
import com.heritage.dto.AnnouncementUpdateRequest;
import com.heritage.dto.AnnouncementVO;
import com.heritage.dto.PageQuery;
import com.heritage.entity.Announcement;
import com.heritage.mapper.AnnouncementMapper;
import com.heritage.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement>
        implements AnnouncementService {

    @Override
    public Long createAnnouncement(AnnouncementCreateRequest request) {
        Announcement entity = new Announcement();
        entity.setTitle(request.getTitle().trim());
        entity.setContent(request.getContent());
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
    public void updateAnnouncement(Long id, AnnouncementUpdateRequest request) {
        Announcement existing;
        try {
            existing = this.getById(id);
        } catch (DataAccessException e) {
            throw new BusinessException("数据未初始化");
        }
        if (existing == null) {
            throw new BusinessException("公告不存在");
        }

        if (request.getTitle() != null) {
            existing.setTitle(request.getTitle().trim());
        }
        if (request.getContent() != null) {
            existing.setContent(request.getContent());
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
    public void deleteAnnouncement(Long id) {
        Announcement existing;
        try {
            existing = this.getById(id);
        } catch (DataAccessException e) {
            throw new BusinessException("数据未初始化");
        }
        if (existing == null) {
            throw new BusinessException("公告不存在");
        }

        try {
            this.removeById(id);
        } catch (DataAccessException e) {
            throw new BusinessException("删除失败");
        }
    }

    @Override
    public Page<AnnouncementVO> getManagePage(PageQuery query, Integer status) {
        Page<Announcement> page = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(Announcement::getStatus, status);
        }
        if (query.getKeyword() != null && !query.getKeyword().trim().isEmpty()) {
            wrapper.like(Announcement::getTitle, query.getKeyword().trim());
        }

        wrapper.orderByDesc(Announcement::getIsTop)
                .orderByDesc(Announcement::getCreateTime);

        Page<Announcement> entityPage;
        try {
            entityPage = this.page(page, wrapper);
        } catch (DataAccessException e) {
            Page<AnnouncementVO> empty = new Page<>(query.getPage(), query.getSize(), 0);
            empty.setRecords(Collections.emptyList());
            return empty;
        }

        Page<AnnouncementVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(entityPage.getRecords().stream().map(this::toVO).toList());
        return voPage;
    }

    @Override
    public Page<AnnouncementVO> getPublishedPage(PageQuery query) {
        Page<Announcement> page = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Announcement::getStatus, 1)
                .orderByDesc(Announcement::getIsTop)
                .orderByDesc(Announcement::getCreateTime);

        Page<Announcement> entityPage;
        try {
            entityPage = this.page(page, wrapper);
        } catch (DataAccessException e) {
            Page<AnnouncementVO> empty = new Page<>(query.getPage(), query.getSize(), 0);
            empty.setRecords(Collections.emptyList());
            return empty;
        }

        Page<AnnouncementVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(entityPage.getRecords().stream().map(this::toVO).toList());
        return voPage;
    }

    @Override
    public AnnouncementVO getDetail(Long id) {
        Announcement entity;
        try {
            entity = this.getById(id);
        } catch (DataAccessException e) {
            return null;
        }
        if (entity == null) {
            throw new BusinessException("公告不存在");
        }
        return toVO(entity);
    }

    private AnnouncementVO toVO(Announcement entity) {
        AnnouncementVO vo = new AnnouncementVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
