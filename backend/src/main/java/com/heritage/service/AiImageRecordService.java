package com.heritage.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.heritage.dto.AiImageRecordVO;
import com.heritage.dto.PageQuery;
import com.heritage.entity.AiImageRecord;

import java.util.List;

public interface AiImageRecordService extends IService<AiImageRecord> {

    void saveRecords(Long userId, String originalImageUrl, String prompt, List<String> resultImageUrls);

    Page<AiImageRecordVO> pageUserRecords(Long userId, PageQuery query);
}

