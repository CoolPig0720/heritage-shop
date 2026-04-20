package com.heritage.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.heritage.dto.HeritageStoryCreateRequest;
import com.heritage.dto.HeritageStoryUpdateRequest;
import com.heritage.dto.HeritageStoryVO;
import com.heritage.dto.PageQuery;
import com.heritage.entity.HeritageStory;

public interface HeritageStoryService extends IService<HeritageStory> {

    Long createHeritageStory(HeritageStoryCreateRequest request);

    void updateHeritageStory(Long id, HeritageStoryUpdateRequest request);

    void deleteHeritageStory(Long id);

    Page<HeritageStoryVO> getManagePage(PageQuery query, Integer status, Long heritageProjectId);

    Page<HeritageStoryVO> getPublishedPage(PageQuery query);

    HeritageStoryVO getDetail(Long id);
}
