package com.heritage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heritage.dto.HeritageInheritorAdminVO;
import com.heritage.dto.HeritageInheritorCreateRequest;
import com.heritage.dto.HeritageInheritorDetailVO;
import com.heritage.dto.HeritageInheritorSimpleVO;
import com.heritage.dto.HeritageInheritorUpdateRequest;
import com.heritage.dto.PageQuery;
import com.heritage.entity.HeritageInheritor;

public interface HeritageInheritorService extends IService<HeritageInheritor> {

    HeritageInheritorDetailVO getInheritorDetail(Long inheritorId);

    Page<HeritageInheritorSimpleVO> pageInheritors(PageQuery query);

    Page<HeritageInheritorAdminVO> pageAdminInheritors(PageQuery query);

    HeritageInheritorAdminVO getAdminInheritor(Long inheritorId);

    Long createInheritor(HeritageInheritorCreateRequest request);

    void updateInheritor(Long inheritorId, HeritageInheritorUpdateRequest request);

    void deleteInheritor(Long inheritorId);
}
