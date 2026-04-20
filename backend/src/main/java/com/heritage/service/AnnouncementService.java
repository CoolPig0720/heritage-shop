package com.heritage.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.heritage.dto.AnnouncementCreateRequest;
import com.heritage.dto.AnnouncementUpdateRequest;
import com.heritage.dto.AnnouncementVO;
import com.heritage.dto.PageQuery;
import com.heritage.entity.Announcement;

public interface AnnouncementService extends IService<Announcement> {

    Long createAnnouncement(AnnouncementCreateRequest request);

    void updateAnnouncement(Long id, AnnouncementUpdateRequest request);

    void deleteAnnouncement(Long id);

    Page<AnnouncementVO> getManagePage(PageQuery query, Integer status);

    Page<AnnouncementVO> getPublishedPage(PageQuery query);

    AnnouncementVO getDetail(Long id);
}
