package com.heritage.service;

import com.heritage.dto.CustomizeMessageCreateRequest;
import com.heritage.dto.CustomizeMessageVO;

import java.util.List;

public interface CustomizeMessageService {

    Long sendMessage(Long userId, String role, Long requestId, CustomizeMessageCreateRequest request);

    List<CustomizeMessageVO> listMessages(Long userId, Long requestId);

    void markAsRead(Long userId, Long requestId);

    Long countUnreadMessages(Long userId);
}
