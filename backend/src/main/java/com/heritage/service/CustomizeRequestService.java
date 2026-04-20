package com.heritage.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.heritage.dto.*;
import com.heritage.entity.CustomizeRequest;

import java.util.List;

public interface CustomizeRequestService extends IService<CustomizeRequest> {

    List<MerchantCardVO> listMerchants();

    Page<MerchantCardVO> pageMerchants(PageQuery query);

    CustomizeUnreadVO getUnreadCount(Long userId, String role);

    Page<ProductVO> pageMerchantProducts(Long merchantId, PageQuery query);

    Long createRequest(Long userId, CustomizeRequestCreateRequest request);

    Page<CustomizeRequestVO> pageMyRequests(Long userId, PageQuery query, String status, String keyword);

    Page<CustomizeRequestVO> pageReceivedRequests(Long merchantId, PageQuery query, String status, String keyword);

    CustomizeRequestVO getDetail(Long userId, Long id);

    void quote(Long merchantId, Long id, CustomizeQuoteRequest request);

    Long confirm(Long userId, Long id, CustomizeConfirmRequest request);

    void updateStatus(Long userId, String role, Long id, CustomizeStatusUpdateRequest request);
}
