package com.heritage.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.heritage.dto.HeritageProjectDetailVO;
import com.heritage.dto.HeritageProjectListItemVO;
import com.heritage.dto.HeritageProjectMediaCreateRequest;
import com.heritage.dto.HeritageProjectMediaUpdateRequest;
import com.heritage.dto.HeritageProjectCreateRequest;
import com.heritage.dto.HeritageProjectUpdateRequest;
import com.heritage.dto.PageQuery;
import com.heritage.entity.HeritageProject;

import java.util.List;

public interface HeritageProjectService extends IService<HeritageProject> {

    Page<HeritageProjectListItemVO> pageProjects(PageQuery query, Long categoryId);

    HeritageProjectDetailVO getProjectDetail(Long projectId);

    Long createProject(HeritageProjectCreateRequest request);

    void updateProject(Long projectId, HeritageProjectUpdateRequest request);

    void updateProjectInheritors(Long projectId, List<Long> inheritorIds);

    Long addProjectMedia(Long projectId, HeritageProjectMediaCreateRequest request);

    void updateProjectMedia(Long mediaId, HeritageProjectMediaUpdateRequest request);

    void deleteProjectMedia(Long mediaId);

    void deleteProject(Long projectId);
}
