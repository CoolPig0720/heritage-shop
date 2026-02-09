package com.heritage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heritage.dto.AiImageRecordVO;
import com.heritage.dto.PageQuery;
import com.heritage.entity.AiImageRecord;
import com.heritage.mapper.AiImageRecordMapper;
import com.heritage.service.AiImageRecordService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AiImageRecordServiceImpl extends ServiceImpl<AiImageRecordMapper, AiImageRecord> implements AiImageRecordService {

    @Override
    @Transactional
    public void saveRecords(Long userId, String originalImageUrl, String prompt, List<String> resultImageUrls) {
        if (userId == null || resultImageUrls == null || resultImageUrls.isEmpty()) {
            return;
        }

        for (String url : resultImageUrls) {
            if (url == null || url.isBlank()) {
                continue;
            }
            AiImageRecord record = new AiImageRecord();
            record.setUserId(userId);
            record.setOriginalImageUrl(originalImageUrl);
            record.setPrompt(prompt);
            record.setResultImageUrl(url);
            this.save(record);
        }
    }

    @Override
    public Page<AiImageRecordVO> pageUserRecords(Long userId, PageQuery query) {
        int page = query == null || query.getPage() == null ? 1 : Math.max(1, query.getPage());
        int size = query == null || query.getSize() == null ? 10 : Math.max(1, query.getSize());

        Page<AiImageRecord> mpPage = new Page<>(page, size);
        LambdaQueryWrapper<AiImageRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiImageRecord::getUserId, userId);
        wrapper.orderByDesc(AiImageRecord::getId);
        Page<AiImageRecord> recordPage = this.page(mpPage, wrapper);

        Page<AiImageRecordVO> voPage = new Page<>();
        voPage.setCurrent(recordPage.getCurrent());
        voPage.setSize(recordPage.getSize());
        voPage.setTotal(recordPage.getTotal());
        voPage.setPages(recordPage.getPages());
        voPage.setRecords(recordPage.getRecords().stream().map(r -> {
            AiImageRecordVO vo = new AiImageRecordVO();
            BeanUtils.copyProperties(r, vo);
            return vo;
        }).toList());
        return voPage;
    }
}

